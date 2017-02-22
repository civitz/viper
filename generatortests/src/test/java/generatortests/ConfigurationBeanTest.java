package generatortests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

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
	
}
