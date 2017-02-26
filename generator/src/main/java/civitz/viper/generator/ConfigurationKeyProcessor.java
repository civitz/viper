package civitz.viper.generator;

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
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import civitz.viper.CdiConfiguration;
import civitz.viper.CdiConfiguration.PassAnnotations;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "civitz.viper.CdiConfiguration" })
public class ConfigurationKeyProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		
		super.init(processingEnv);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		processingEnv.getMessager().printMessage(Kind.NOTE, "called getSupportedAnnotationTypes");
		return Sets.newHashSet(CdiConfiguration.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		processingEnv.getMessager().printMessage(Kind.NOTE, "called getSupportedSourceVersion");
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// TODO Auto-generated method stub
		processingEnv.getMessager().printMessage(Kind.NOTE, "called processing");
		Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(CdiConfiguration.class);
		if (elementsAnnotatedWith.size() > 1) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "more than one element per type CdiConfiguration");
		}
		for (Element e : elementsAnnotatedWith) {
			if (e.getKind() == ElementKind.ENUM) {
				try {

					TypeElement classElement = (TypeElement) e;
					PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

					String className = classElement.getSimpleName().toString();

					CdiConfiguration annotation = classElement.getAnnotation(CdiConfiguration.class);
					String propertiesPath = annotation.propertiesPath() != null ? annotation.propertiesPath()
							: "/opt/" + className + "/mama.properties";

					List<String> passedAnnotations = getPassedAnnotations(classElement);
					String packageName = packageElement.getQualifiedName().toString();

					
					Builder<String, Object> builder = ImmutableMap.<String, Object> builder()
						.put("enumClass", className)
						.put("packageName", packageName)
						.put("propertiesPath", propertiesPath)
						.put("passedAnnotations", passedAnnotations);
					
					getValidatorMethod(classElement).ifPresent(method -> {
						builder.put("validator", method);
					});
					
					getKeyStringMethod(classElement).ifPresent(method -> {
						builder.put("keyString", method);
					});
					
					String keyNull = getKeyNullValue(classElement).orElseGet(() -> getFirstEnumConstant(classElement));
					builder.put("nullValue", keyNull);

					ImmutableMap<String, Object> props = builder.build();
					
					Template config = generateTemplateFor("Configuration.vm", props);
					JavaFileObject configSourceFile = processingEnv.getFiler()
							.createSourceFile(packageName + ".Configuration", e);
					Writer configWriter = configSourceFile.openWriter();
					config.merge(contextFromProperties(props), configWriter);
					configWriter.flush();
					configWriter.close();

					JavaFileObject configBeanSourceFile = processingEnv.getFiler()
							.createSourceFile(packageName + ".ConfigurationBean", e);
					Template configBean = generateTemplateFor("ConfigurationBean.vm", props);
					Writer configBeanWriter = configBeanSourceFile.openWriter();
					configBean.merge(contextFromProperties(props), configBeanWriter);
					configBeanWriter.flush();
					configBeanWriter.close();
				} catch (IOException e1) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "error creating file", e);
				}

			} else {
				processingEnv.getMessager().printMessage(Kind.ERROR, "not an enum type", e);
			}
		}

		return true;
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
	
	private Optional<String> getKeyNullValue(TypeElement classElement) {
		return classElement.getEnclosedElements()
				.stream()
				.filter(x -> x.getKind() == ElementKind.ENUM_CONSTANT)
				.filter(x -> x.getAnnotation(CdiConfiguration.KeyNullValue.class) != null)
				.map(x -> x.getSimpleName().toString())
				.findFirst();
	}
	
	private Optional<String> getKeyStringMethod(TypeElement classElement) {
		return classElement.getEnclosedElements()
			.stream()
			.filter(x -> x.getKind() == ElementKind.METHOD)
			.filter(x -> x.getAnnotation(CdiConfiguration.KeyString.class) != null)
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
			.filter(entry->entry.getKey().getSimpleName().toString().equals("value"))
			.map(Entry::getValue)
			.findFirst();
		
		if(!value.isPresent()){
			return Lists.newArrayList();
		}
		
		@SuppressWarnings("unchecked")
		List<Object> annotations = (List<Object>) value.get().getValue();
		return	annotations.stream()
			.map(Object::toString)
			.map(s->s.endsWith(".class")?s.substring(0, s.length()-6):s)
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
