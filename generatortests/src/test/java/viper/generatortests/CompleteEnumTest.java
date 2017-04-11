package viper.generatortests;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static viper.generatortests.utils.MethodPredicates.hasAnnotations;
import static viper.generatortests.utils.MethodPredicates.hasName;
import static viper.generatortests.utils.MethodPredicates.hasParametersOfType;
import static viper.generatortests.utils.MethodPredicates.hasReturnType;
import static viper.generatortests.utils.MethodPredicates.isStatic;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.assertj.core.api.Condition;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;

import viper.ConfigurationResolver;
import viper.generator.ConfigurationKeyProcessor;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompleteEnumTest {
	
	@ClassRule
	public static TemporaryFolder temp = new TemporaryFolder();
	
	@Rule
	public JUnitSoftAssertions softly = new JUnitSoftAssertions();
	
	private static final String packageName = CompleteEnum.class.getPackage().getName();
	
	public static boolean annotationProcessorHasRunSuccesfully = false;

	private static File sourceDir;
	private static File outputDir;
	private static File generatedSourcesDir;
	
	URLClassLoader outputClassLoader;

	@BeforeClass
	public static void setUpClass() throws Exception{
		sourceDir = temp.newFolder("sources");
		outputDir = temp.newFolder("classes");
		generatedSourcesDir = temp.newFolder("generated");
	}
	
	@Before
	public void setUp() throws Exception {
		outputClassLoader = getClassLoaderForCompiledFiles();
	}
	
	@After
	public void tearDown() throws Exception {
		outputClassLoader.close();
	}
	
	@Test // starts with "_" for method ordering
	public void _shouldRunAnnotationProcessor() throws Exception {
		final String enumToCompile = "CompleteEnum.java";

		/*
		 * Note to future dev: this is intended both as a test and as a way to
		 * debug future versions of the annotation processor.
		 * 
		 * You can use it to test different enum variation, and my hope is to
		 * extend the tests by putting "java" files in resources directory, and
		 * manually call the compiler here.
		 * 
		 */

		Path source = Paths.get("src", "test", "java", "viper", "generatortests", enumToCompile);
		Path destinationDir = Paths.get(sourceDir.getAbsolutePath(), "viper", "generatortests");
		Files.createDirectories(destinationDir);
		Files.copy(source, destinationDir.resolve(enumToCompile));

		// combine compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		fileManager.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(sourceDir));
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputDir));
		fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(generatedSourcesDir));

		// get files to be compiled
		Iterable<JavaFileObject> files = fileManager.list(StandardLocation.SOURCE_PATH, "",
				Collections.singleton(Kind.SOURCE), true);

		// setup compilation task
		CompilationTask task = compiler.getTask(new PrintWriter(System.out), fileManager, null, null, null, files);
		task.setProcessors(Arrays.asList(new ConfigurationKeyProcessor()));

		// compile
		Boolean result = task.call();

		assertThat(result)
			.as("Compile task should work on complete enum")
			.isTrue();

		List<String> javaGeneratedFiles = Files.walk(generatedSourcesDir.toPath())
			.filter(p->p.toFile().isFile())
			.filter(p->p.getFileName().toString().endsWith(".java"))
			.map(p->p.getFileName())
			.map(p->p.toString())
			.collect(toList());

		assertThat(javaGeneratedFiles)
			.as("Chould generate annotation, configuration bean, and property file config resolver")
			.contains("ConfigurationBean.java", "Configuration.java", "PropertyFileConfigurationResolver.java");
		annotationProcessorHasRunSuccesfully = true;
	}
	
	@Test
	public void shouldGenerateConfigurationBeanClass() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = packageName + ".ConfigurationBean";
		Class<?> confBeanClass = tryLoadClassFromCompiledFiles(className).orElse(null);
		assertThat(confBeanClass)
			.isNotNull()
			.isNotAnnotation();
		
		Field field = confBeanClass.getDeclaredField("resolver");
		assertThat(field).isNotNull();
		
		softly.assertThat(field.getType())
			.as("Should have a resolver field of type " + ConfigurationResolver.class.getSimpleName())
			.isEqualTo(ConfigurationResolver.class);
		Type[] actualTypeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
		softly.assertThat(actualTypeArguments)
			.as("Should have a resolver field of generic type " + CompleteEnum.class.getSimpleName())
			.containsExactly(CompleteEnum.class);
		
		softly.assertThat(field.getAnnotations())
			.hasSize(1);
		softly.assertThat(field.getAnnotations()[0].annotationType())
			.as("Should have an inject-able resolver")
			.isEqualTo(Inject.class);
	}
	
	@Test
	public void shouldGenerateAllProducerMethodsForConfigurationBean() throws Exception {
		assumingAnnotationProcessorHasRun();
		
		String beanClassName = packageName + ".ConfigurationBean";
		String annotationClassName = packageName + ".Configuration";

		Optional<Class<?>> beanClass = tryLoadClassFromCompiledFiles(beanClassName);
		Optional<Class<? extends Annotation>> annotationClass = tryLoadClassFromCompiledFiles(annotationClassName);

		assumeTrue("No Configuration annotation present, skipping test...", annotationClass.isPresent());
		assumeTrue("No ConfigurationBean class present, skipping test...", beanClass.isPresent());
		

		final Class<? extends Annotation> generatedAnnotation = annotationClass.get();
		
		softly.assertThat(beanClass.get().getDeclaredMethods())
			.has(aMethodThat("validates a property", 
					hasName("isValid"), 
					hasParametersOfType(CompleteEnum.class, String.class),
					isStatic()))
			.has(aMethodThat("formats a message for missing properties", 
					hasName("formatMissing"),
					hasParametersOfType(CompleteEnum.class),
					isStatic().negate()))
			.has(aMethodThat("formats a message for invalid properties", 
					hasName("formatInvalid"),
					hasParametersOfType(CompleteEnum.class, String.class),
					isStatic().negate()))
			.has(aCdiProducerMethod(generatedAnnotation, "getStringProperty", String.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getByteProperty", Byte.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getShortProperty", Short.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getIntegerProperty", Integer.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getLongProperty", Long.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getFloatProperty", Float.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getDoubleProperty", Double.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getBooleanProperty", Boolean.class))
			.has(aCdiProducerMethod(generatedAnnotation, "getCharacterProperty", Character.class));
	}
	
	@Test
	public void shouldGenerateConfigurationAnnotation() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = packageName + ".Configuration";
		Class<?> confBeanClass = tryLoadClassFromCompiledFiles(className).orElse(null);
		assertThat(confBeanClass)
			.isAnnotation()
			.isNotNull();
		
		softly.assertThat(confBeanClass.getDeclaredMethods())
			.filteredOn(m -> m.getName().equals("value"))
			.filteredOn(m -> m.getParameterCount() == 0)
			.filteredOn(m -> m.getReturnType().equals(CompleteEnum.class))
			.as("Configuration annotation should have a value method of type " + CompleteEnum.class.getName())
			.hasSize(1);
	}
	
	@Test
	public void shouldGeneratePropertyFileConfigurationResolver() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = packageName + ".PropertyFileConfigurationResolver";
		Class<?> confBeanClass = tryLoadClassFromCompiledFiles(className).orElse(null);
		assertThat(confBeanClass)
			.isNotAnnotation()
			.isNotNull();
		
		softly.assertThat(ConfigurationResolver.class.isAssignableFrom(confBeanClass))
			.as("Generated file should be an interface extending " + ConfigurationResolver.class.getSimpleName())
			.isTrue();
		
		softly.assertThat(confBeanClass.getDeclaredMethods())
			.has(aMethodThat("gets the configuration value from an enum constant", 
					hasName("getConfigurationValue"),
					isStatic().negate(),
					hasParametersOfType(CompleteEnum.class),
					hasReturnType(String.class)));
	}

	@Test
	public void shouldAddGeneratedAnnotationToGeneratedSourceCode_propertyFileResolver() throws Exception {
		assumingAnnotationProcessorHasRun();
		final File generatedPropertyResolver = findFileInGeneratedSources("PropertyFileConfigurationResolver.java");
		CompilationUnit compilationUnit = JavaParser.parse(generatedPropertyResolver);
		TypeDeclaration typeDeclaration = compilationUnit.getTypes().get(0);

		assertThat(typeDeclaration.getAnnotations())
				.has(aGeneratedAnnotationWithValue(ConfigurationKeyProcessor.class.getCanonicalName()));
	}

	@Test
	public void shouldAddGeneratedAnnotationToGeneratedSourceCode_configurationBean() throws Exception {
		assumingAnnotationProcessorHasRun();
		File generatedConfigurationBean = findFileInGeneratedSources("ConfigurationBean.java");
		CompilationUnit compilationUnit = JavaParser.parse(generatedConfigurationBean);
		TypeDeclaration typeDeclaration = compilationUnit.getTypes().get(0);

		assertThat(typeDeclaration.getAnnotations())
			.has(aGeneratedAnnotationWithValue(ConfigurationKeyProcessor.class.getCanonicalName()));
	}

	@Test
	public void shouldAddGeneratedAnnotationToGeneratedSourceCode_configurationAnnotation() throws Exception {
		assumingAnnotationProcessorHasRun();
		File generatedConfigurationBean = findFileInGeneratedSources("Configuration.java");
		CompilationUnit compilationUnit = JavaParser.parse(generatedConfigurationBean);
		TypeDeclaration typeDeclaration = compilationUnit.getTypes().get(0);

		assertThat(typeDeclaration.getAnnotations())
			.has(aGeneratedAnnotationWithValue(ConfigurationKeyProcessor.class.getCanonicalName()));
	}

	public File findFileInGeneratedSources(String classFileName) throws AssertionError, IOException {
		Path classPath = Files.find(generatedSourcesDir.toPath(), 10, (file,attr)-> file.getFileName().toString().equals(classFileName))
			.findFirst()
			.orElseThrow(()->new AssertionError("no " + classFileName + " found"));
		final File file = classPath.toFile();
		return file;
	}

	private Condition<? super List<? extends AnnotationExpr>> aGeneratedAnnotationWithValue(String value) {
		Predicate<? super List<? extends AnnotationExpr>> predicate = annotations -> annotations.stream()
				.filter(a -> a.getName().toStringWithoutComments().equals("Generated"))
				.anyMatch(
						a -> a.getChildrenNodes().stream().anyMatch(n -> n.toStringWithoutComments().contains(value)));
		return new Condition<>(predicate, "a @Generated annotation with value " + value);
	}

	@SafeVarargs
	static Condition<Method[]> aMethodThat(String description, Predicate<Method> first, Predicate<Method>... predicates){
		final Predicate<Method> combinedPredicate = Arrays.stream(predicates)
				.reduce(first, Predicate::and);
		Predicate<Method[]> methodsPredicate = methods -> Arrays.stream(methods)
					.filter(combinedPredicate)
					.count() == 1;
		return new Condition<Method[]>(methodsPredicate, "a method that " + description);
	}
	
	public Condition<Method[]> aCdiProducerMethod(final Class<? extends Annotation> generatedAnnotation,
			final String methodName, final Class<?> producerType) {
		return aMethodThat("produces "+ producerType.getSimpleName() + " properties", 
				hasName(methodName),
				hasParametersOfType(InjectionPoint.class),
				hasReturnType(producerType),
				hasAnnotations(Produces.class, generatedAnnotation));
	}

	public URLClassLoader getClassLoaderForCompiledFiles() throws MalformedURLException {
		return new URLClassLoader(new URL[] { outputDir.toURI().toURL() });
	}
	
	@SuppressWarnings("unchecked")
	public <T>  Class<? extends T> loadClassFromCompiledFiles(String className) throws ClassNotFoundException, IOException {
		return (Class<? extends T>) outputClassLoader.loadClass(className);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<Class<? extends T>> tryLoadClassFromCompiledFiles(String className) throws ClassNotFoundException, IOException {
		try {
			return Optional.of((Class<? extends T>)loadClassFromCompiledFiles(className));
		}catch(ClassNotFoundException e){
			return Optional.empty();
		}
	}

	/*
	 * I know, this method is a really ugly way to skip tests if annotation
	 * processor didn't run. But it works.
	 * Also, I tried with JExample, but it didn't work well with @ClassRule
	 */
	public void assumingAnnotationProcessorHasRun() {
		assumeTrue("Annotation should have run succesfully for this test to run...",
				annotationProcessorHasRunSuccesfully);
	}
	
	
	
}
