package viper.generator;

import static viper.generator.ConfigurationKeyProcessor.calculateAnnotationName;
import static viper.generator.ConfigurationKeyProcessor.calculateConfigurationBeanName;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationKeyProcessorTest {

	private static final String ENUM_CLASS = "EnumBase";
	@Rule
	public JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Test
	public void shouldFailForNullString_annotationName() throws Exception {
		softly.assertThatThrownBy(() -> calculateAnnotationName(null, null))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName("Clazz", null))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName(null, "ClazzAnnot"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void shouldFailForInvalidString_annotationName() throws Exception {
		softly.assertThatThrownBy(() -> calculateAnnotationName(ENUM_CLASS, ""))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName(ENUM_CLASS, "**yo"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName(ENUM_CLASS, "yo***"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName(ENUM_CLASS, "*yo*"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateAnnotationName(ENUM_CLASS, "ma**yo"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void shouldReplaceWithEnumName_annotationName() throws Exception {
		softly.assertThat(calculateAnnotationName(ENUM_CLASS, "*Annotation")).isEqualTo(ENUM_CLASS + "Annotation");
		softly.assertThat(calculateAnnotationName(ENUM_CLASS, "Annotation*")).isEqualTo("Annotation" + ENUM_CLASS);
		softly.assertThat(calculateAnnotationName(ENUM_CLASS, "Anno*Tation")).isEqualTo("Anno" + ENUM_CLASS + "Tation");
	}

	@Test
	public void shouldGenerateExactName_annotatioName() throws Exception {
		softly.assertThat(calculateAnnotationName(ENUM_CLASS, "EnumBaseAnnotation")).isEqualTo("EnumBaseAnnotation");
		softly.assertThat(calculateAnnotationName(ENUM_CLASS, "CustomAnnotation")).isEqualTo("CustomAnnotation");
	}

	@Test
	public void shouldFailForNullString_configurationBeanName() throws Exception {
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(null, null))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName("Clazz", null))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(null, "ClazzAnnot"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void shouldFailForInvalidString_configurationBeanName() throws Exception {
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(ENUM_CLASS, ""))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(ENUM_CLASS, "**yo"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(ENUM_CLASS, "yo***"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(ENUM_CLASS, "*yo*"))
				.isInstanceOf(IllegalArgumentException.class);
		softly.assertThatThrownBy(() -> calculateConfigurationBeanName(ENUM_CLASS, "ma**yo"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void shouldReplaceWithEnumName_configurationBeanName() throws Exception {
		softly.assertThat(calculateConfigurationBeanName(ENUM_CLASS, "*ConfBean")).isEqualTo(ENUM_CLASS + "ConfBean");
		softly.assertThat(calculateConfigurationBeanName(ENUM_CLASS, "ConfBean*")).isEqualTo("ConfBean" + ENUM_CLASS);
		softly.assertThat(calculateConfigurationBeanName(ENUM_CLASS, "Anno*Tation"))
				.isEqualTo("Anno" + ENUM_CLASS + "Tation");
	}

	@Test
	public void shouldGenerateExactName_configurationBeanName() throws Exception {
		softly.assertThat(calculateConfigurationBeanName(ENUM_CLASS, "EnumBaseConfBean")).isEqualTo("EnumBaseConfBean");
		softly.assertThat(calculateConfigurationBeanName(ENUM_CLASS, "CustomConfBean")).isEqualTo("CustomConfBean");
	}
}
