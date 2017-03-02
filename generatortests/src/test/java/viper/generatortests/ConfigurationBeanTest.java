package viper.generatortests;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import viper.ConfigurationResolver;
import viper.generator.ConfigurationKeyProcessor;

import java.io.File;
import java.io.PrintWriter;
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


public class ConfigurationBeanTest {
	
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	
	@Test
	public void shouldGeneratedConfigurationBeanClass() throws Exception {
		String className = CompleteEnum.class.getPackage().getName() + ".ConfigurationBean";
		Class<?> confBeanClass = Class.forName(className);
		assertThat(confBeanClass)
			.isNotAnnotation()
			.isNotNull();
	}
	
	@Test
	public void shouldGeneratedConfigurationAnnotation() throws Exception {
		String className = CompleteEnum.class.getPackage().getName() + ".Configuration";
		Class<?> confBeanClass = Class.forName(className);
		assertThat(confBeanClass)
			.isAnnotation()
			.isNotNull();
	}
	
	@Test
	public void shouldGeneratedPropertyFileConfigurationResolver() throws Exception {
		String className = CompleteEnum.class.getPackage().getName() + ".PropertyFileConfigurationResolver";
		Class<?> confBeanClass = Class.forName(className);
		assertThat(confBeanClass)
			.isNotAnnotation()
			.isNotNull();
		
		assertThat(ConfigurationResolver.class.isAssignableFrom(confBeanClass))
			.as("Generated file should be an interface extending " + ConfigurationResolver.class.getSimpleName())
			.isTrue();
	}

	@Test
	public void shouldRunAnnotationProcessor() throws Exception {
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
		
		File sourceDir = temp.newFolder("sources");
		File outputDir = temp.newFolder("classes");
		File generatedSourcesDir = temp.newFolder("generated");

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
			.as("Compile task should work on example enum")
			.isTrue();

		List<String> javaGeneratedFiles = Files.walk(generatedSourcesDir.toPath())
			.filter(p->p.toFile().isFile())
			.filter(p->p.getFileName().toString().endsWith(".java"))
			.map(p->p.getFileName())
			.map(p->p.toString())
			.collect(toList());

		assertThat(javaGeneratedFiles)
			.contains("ConfigurationBean.java", "Configuration.java", "PropertyFileConfigurationResolver.java");
	}
	
}
