package viper.generatortests;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Lists;

import viper.generator.ConfigurationKeyProcessor;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultipleEnumCoexistenceTest {
	@ClassRule
	public static TemporaryFolder temp = new TemporaryFolder();

	public static boolean annotationProcessorHasRunSuccesfully = false;

	private static File sourceDir;
	private static File outputDir;
	private static File generatedSourcesDir;

	@BeforeClass
	public static void setUpClass() throws Exception{
		sourceDir = temp.newFolder("sources");
		outputDir = temp.newFolder("classes");
		generatedSourcesDir = temp.newFolder("generated");
	}

	final List<String> enumClassesNames = Lists.newArrayList(
			CompleteEnum.class.getSimpleName(),
			SimplestEnum.class.getSimpleName()
		);

	@Test // starts with "_" for method ordering
	public void _shouldRunAnnotationProcessor() throws Exception {
		Path destinationDir = Paths.get(sourceDir.getAbsolutePath(), "viper", "generatortests");
		Files.createDirectories(destinationDir);
		for (String name : enumClassesNames) {
			String enumToCompile = name + ".java";
			Path source = Paths.get("src", "test", "java", "viper", "generatortests", enumToCompile);
			Files.copy(source, destinationDir.resolve(enumToCompile));
		}

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
		annotationProcessorHasRunSuccesfully = true;
	}

	@Test
	public void shouldCompileClassesForCompleteEnum() throws Exception {
		assumingAnnotationProcessorHasRun();
		String enumClassName = CompleteEnum.class.getSimpleName();
		List<String> javaGeneratedFiles = getListOfGeneratedJavaFiles();

		assertThat(javaGeneratedFiles)
			.as("Could generate annotation, configuration bean, "
					+ "and property file config resolver for "
					+ enumClassName)
			.contains(
					"MyConfig.java", // the annotation
					enumClassName + "ConfInjector.java", // the injection bean
					enumClassName + "PropertyFileConfigurationResolver.java" // the property file resolver
				);
	}

	public List<String> getListOfGeneratedJavaFiles() throws IOException {
		return Files.walk(generatedSourcesDir.toPath())
				.filter(p -> p.toFile().isFile())
				.filter(p -> p.getFileName().toString().endsWith(".java"))
				.map(p -> p.getFileName())
				.map(p -> p.toString())
				.collect(toList());
	}

	@Test
	public void shouldCompileClassesForSimplestEnum() throws Exception {
		assumingAnnotationProcessorHasRun();
		String enumClassName = SimplestEnum.class.getSimpleName();
		List<String> javaGeneratedFiles = getListOfGeneratedJavaFiles();

		assertThat(javaGeneratedFiles)
			.as("Could generate annotation, configuration bean, "
					+ "and NO property file config resolver for "
					+ enumClassName)
			.contains(
					enumClassName + "Configuration.java", // the annotation
					enumClassName + "ConfigurationBean.java" // the injection
				)
			.doesNotContain(
					enumClassName + "PropertyFileConfigurationResolver.java" // the property file resolver
				);
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
