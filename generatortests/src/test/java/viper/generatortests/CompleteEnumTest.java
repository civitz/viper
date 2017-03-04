package viper.generatortests;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import viper.ConfigurationResolver;
import viper.generator.ConfigurationKeyProcessor;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompleteEnumTest {
	
	@ClassRule
	public static TemporaryFolder temp = new TemporaryFolder();
	
	public static boolean annotationProcessorHasRunSuccesfully = false;

	private static File sourceDir;
	private static File outputDir;
	private static File generatedSourcesDir;

	@BeforeClass
	public static void setUp() throws Exception{
		sourceDir = temp.newFolder("sources");
		outputDir = temp.newFolder("classes");
		generatedSourcesDir = temp.newFolder("generated");
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
			.contains("ConfigurationBean.java", "Configuration.java", "PropertyFileConfigurationResolver.java");
		annotationProcessorHasRunSuccesfully = true;
	}
	
	@Test
	public void shouldGeneratedConfigurationBeanClass() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = CompleteEnum.class.getPackage().getName() + ".ConfigurationBean";
		Class<?> confBeanClass = loadClassFromCompiledFiles(className);
		assertThat(confBeanClass)
			.isNotAnnotation()
			.isNotNull();
	}

	public Class<?> loadClassFromCompiledFiles(String className) throws ClassNotFoundException, IOException {
		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { outputDir.toURI().toURL() })) {
			Class<?> confBeanClass = classLoader.loadClass(className);
			return confBeanClass;
		}
	}

	/*
	 * I know, this method is a really ugly way to skip tests if annotation
	 * processor hasn't run. But it works.
	 */
	public void assumingAnnotationProcessorHasRun() {
		assumeTrue("Annotation should have run succesfully for this test to run...",
				annotationProcessorHasRunSuccesfully);
	}
	
	@Test
	public void shouldGeneratedConfigurationAnnotation() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = CompleteEnum.class.getPackage().getName() + ".Configuration";
		Class<?> confBeanClass = loadClassFromCompiledFiles(className);
		assertThat(confBeanClass)
			.isAnnotation()
			.isNotNull();
	}
	
	@Test
	public void shouldGeneratedPropertyFileConfigurationResolver() throws Exception {
		assumingAnnotationProcessorHasRun();
		String className = CompleteEnum.class.getPackage().getName() + ".PropertyFileConfigurationResolver";
		Class<?> confBeanClass = loadClassFromCompiledFiles(className);
		assertThat(confBeanClass)
			.isNotAnnotation()
			.isNotNull();
		
		assertThat(ConfigurationResolver.class.isAssignableFrom(confBeanClass))
			.as("Generated file should be an interface extending " + ConfigurationResolver.class.getSimpleName())
			.isTrue();
	}
	
}
