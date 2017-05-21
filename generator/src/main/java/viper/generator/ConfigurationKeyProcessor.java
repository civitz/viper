package viper.generator;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.google.common.base.Throwables;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import viper.CdiConfiguration;
import viper.PropertyFileResolver;
import viper.CdiConfiguration.PassAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "viper.CdiConfiguration", "viper.PropertyFileResolver" })
public class ConfigurationKeyProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Sets.newHashSet(CdiConfiguration.class.getName(), PropertyFileResolver.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(CdiConfiguration.class);
		processingEnv.getMessager().printMessage(Kind.NOTE, String.format("Called processing on elements: %s", elementsAnnotatedWith));
		for (Element e : elementsAnnotatedWith) {
			if (e.getKind() == ElementKind.ENUM) {
				try {
					note(e, "Gathering information for code generation");
					TypeElement classElement = (TypeElement) e;
					PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

					// properties for cdi configuration 
					final String className = classElement.getSimpleName().toString();
					note(e, String.format("Class prefix for generated classes will be \"%s\"", className));
					boolean producersForPrimitives = classElement.getAnnotation(CdiConfiguration.class).producersForPrimitives();
					note(e, "Will " + (producersForPrimitives? "" : "not ") + "generate producers for primitive types");
					List<String> passedAnnotations = getPassedAnnotations(classElement);
					note(e, "Generated configuration producer's class will have the following annotations: " + passedAnnotations);
					String packageName = packageElement.getQualifiedName().toString();
					note(e, String.format("All generated code will be under the \"%s\" package", packageName));

					Builder<String, Object> builder = ImmutableMap.<String, Object> builder()
						.put("generatorName",getClass().getCanonicalName())
						.put("enumClass", className)
						.put("packageName", packageName)
						.put("passedAnnotations", passedAnnotations)
						.put("producersForPrimitives", producersForPrimitives);
					
					getValidatorMethod(classElement).ifPresent(method -> {
						note(e, String.format("Properties will be validated via predicates from the %s method in the enum", method));
						builder.put("validator", method);
					});
					
					Optional<String> propertyFileResolver = Optional.ofNullable(classElement.getAnnotation(PropertyFileResolver.class))
							.map(PropertyFileResolver::propertiesPath);
					propertyFileResolver.ifPresent(path -> {
						note(e, "A property-file based ConfigurationResolver will be generated, reading properties from " + path);
						builder.put("propertiesPath", path);
					});
					getKeyStringMethod(classElement).ifPresent(method -> {
						note(e, "The key string for properties will be the one from the " + method + " method in the enum");
						builder.put("keyString", method);
					});

					String defaultKey = getDefaultEnumKey(classElement).orElseGet(() -> getFirstEnumConstant(classElement));
					note(e, String.format("The default key for the generated annotation will be \"%s\"", defaultKey));
					builder.put("defaultKey", defaultKey);

					ImmutableMap<String, Object> props = builder.build();
					
					// generate cdi configuration
					String configurationAnnotationClassName = packageName + "." + className + "Configuration";
					note(e, "Generating configuration annotation: " + configurationAnnotationClassName);
					generateSourceFileFromTemplate(e, configurationAnnotationClassName, "Configuration.vm", props);

					String configurationBeanClassName = packageName + "." + className + "ConfigurationBean";
					note(e, "Generating configuration producer: " + configurationAnnotationClassName);
					generateSourceFileFromTemplate(e,configurationBeanClassName, "ConfigurationBean.vm",props);

					// generate property file resolver if needed
					if (propertyFileResolver.isPresent()) {
						String configurationResolverClassName = packageName + "." + className + "PropertyFileConfigurationResolver";
						note(e, "Generating property file based configuration resolver: " + configurationResolverClassName);
						generateSourceFileFromTemplate(e, configurationResolverClassName, "ConfigurationResolver.vm", props);
					}
				} catch (IOException e1) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "Error creating files: " + Throwables.getStackTraceAsString(e1), e);
				}
			} else {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Code generation is supported only on enum types", e);
			}
		}
		return true;
	}

	private void generateSourceFileFromTemplate(Element element, String generatedClassName, String templateName, ImmutableMap<String, Object> templateProperties) throws IOException {
		Template config = generateTemplateFor(templateName, templateProperties);
		JavaFileObject configSourceFile = processingEnv.getFiler()
                .createSourceFile(generatedClassName, element);
		Writer configWriter = configSourceFile.openWriter();
		config.merge(contextFromProperties(templateProperties), configWriter);
		configWriter.flush();
		configWriter.close();
	}

	/**
	 * Prints a note prepending the current element name
	 * @param element the current element.
	 * @param message the message.
	 */
	private void note(Element element, String message) {
		// passing the element to printMessage would display the annotated code at each log, which is not what we wanted
		processingEnv.getMessager().printMessage(Kind.NOTE, String.format("%s: %s", element.getSimpleName(), message));
	}

	private String getFirstEnumConstant(TypeElement classElement) {
		return classElement.getEnclosedElements()
			.stream()
			.filter(x -> x.getKind() == ElementKind.ENUM_CONSTANT)
			.map(x -> x.getSimpleName().toString())
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Enum with no constants"));
	}

	private Optional<String> getValidatorMethod(TypeElement classElement) {
		return classElement.getEnclosedElements()
			.stream()
			.filter(x -> x.getKind() == ElementKind.METHOD)
			.filter(x -> x.getAnnotation(CdiConfiguration.ConfigValidator.class) != null)
			.map(x -> x.getSimpleName().toString() + "()")
			.findFirst();
	}
	
	private Optional<String> getDefaultEnumKey(TypeElement classElement) {
		return classElement.getEnclosedElements()
			.stream()
			.filter(x -> x.getKind() == ElementKind.ENUM_CONSTANT)
			.filter(x -> x.getAnnotation(CdiConfiguration.DefaultKey.class) != null)
			.map(x -> x.getSimpleName().toString())
			.findFirst();
	}
	
	private Optional<String> getKeyStringMethod(TypeElement classElement) {
		return classElement.getEnclosedElements()
			.stream()
			.filter(x -> x.getKind() == ElementKind.METHOD)
			.filter(x -> x.getAnnotation(PropertyFileResolver.KeyString.class) != null)
			.map(x -> x.getSimpleName().toString() + "()")
			.findFirst();
	}
	
	private List<String> getPassedAnnotations(TypeElement classElement) {
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(PassAnnotations.class.getCanonicalName());
		TypeMirror passAnnotationsType = typeElement.asType();
		Optional<? extends AnnotationMirror> mirror = classElement.getAnnotationMirrors()
			.stream()
			.filter(one -> one.getAnnotationType().equals(passAnnotationsType))
			.findAny();
		
		if(!mirror.isPresent()){
			return Lists.newArrayList();
		}
		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.get().getElementValues();
		Optional<? extends AnnotationValue> value = elementValues.entrySet().stream()
			.filter(entry -> entry.getKey().getSimpleName().toString().equals("value"))
			.map(Entry::getValue)
			.findFirst();
		
		if(!value.isPresent()){
			return Lists.newArrayList();
		}
		
		@SuppressWarnings("unchecked")
		List<Object> annotations = (List<Object>) value.get().getValue();
		return	annotations.stream()
			.map(Object::toString)
			.map(s -> s.endsWith(".class") ? s.substring(0, s.length() - 6) : s)
			.collect(toList());
	}
	
	Template generateTemplateFor(String templateName, Map<String, Object> properties) throws IOException {
		Properties props = new Properties();
		URL url = this.getClass().getClassLoader().getResource("velocity.properties");
		props.load(url.openStream());

		VelocityEngine ve = new VelocityEngine(props);
		ve.init();
		return ve.getTemplate(templateName);
	}

	VelocityContext contextFromProperties(Map<String, Object> properties) {
		VelocityContext vc = new VelocityContext();
		for (Entry<String, Object> e : properties.entrySet()) {
			vc.put(e.getKey(), e.getValue());
		}
		return vc;
	}
}
