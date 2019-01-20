package viper.cditests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NoCdiEnumTest {
	private static final int CUSTOM_AGE = 42;
	private static final String CUSTOM_NAME = "Obama";
	private static final String CUSTOM_PROPERTIES_FILENAME = "custom.properties";
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	private String customPath;

	@Before
	public void setup() throws Exception {
		Properties props = new Properties();
		props.setProperty(NoCdiEnum.AGE.name().toLowerCase(), "42");
		props.setProperty(NoCdiEnum.NAME.name().toLowerCase(), CUSTOM_NAME);
		File customPropertiesFile = temp.newFile(CUSTOM_PROPERTIES_FILENAME);
		customPath = customPropertiesFile.getAbsolutePath();
		props.store(new FileOutputStream(customPropertiesFile), null);
	}

	@Test
	public void shouldReadFromTempDirUsingManualInstantiation() throws Exception {
		NoCdiEnumConfigurationBean configurationBean = NoCdiEnumConfigurationBean
				.create(NoCdiEnumPropertyFileConfigurationResolver.create(customPath));
		assertThat(configurationBean.getProperty(NoCdiEnum.NAME)).isEqualTo(CUSTOM_NAME);
		assertThat(configurationBean.getIntegerProperty(NoCdiEnum.AGE)).isEqualTo(CUSTOM_AGE);
	}
}
