package viper.generatortests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import viper.ConfigurationResolver;
import viper.generator.ConfigurationKeyProcessor;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;


public class ConfigurationBeanTest {
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
     public void runAnnoationProcessor() throws Exception {
             String source = "src/test/";

             Iterable<JavaFileObject> files = getSourceFiles(source);

             JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

             CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
             task.setProcessors(Arrays.asList(new ConfigurationKeyProcessor()));

             task.call();
     }

     private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
             JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
             StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);
             files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

             Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
             return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
     }

	
}
