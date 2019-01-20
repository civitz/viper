package viper.cditests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import viper.ConfigurationResolver;

@RunWith(Arquillian.class)
public class EnumWithPropertiesFileSyspropTest {

	private static final String EXPECTED_NAME = "Mambo";
	private static final String EXPECTED_AGE = "5";

	@Inject
	@EnumWithPropertiesFileConfiguration(EnumWithPropertiesFile.NAME)
	Instance<String> name;

	@Inject
	@EnumWithPropertiesFileConfiguration(EnumWithPropertiesFile.AGE)
	Instance<String> age;

	@ClassRule
	public static TemporaryFolder temp = new TemporaryFolder();

	@BeforeClass
	public static void setUp() throws Exception {
		// This is complementary to EnumWithPropertiesFileTest
		File tempProps = temp.newFile("some.custom.props");
		Properties properties = new Properties();
		properties.setProperty(EnumWithPropertiesFile.NAME.name().toLowerCase(), EXPECTED_NAME);
		properties.setProperty(EnumWithPropertiesFile.AGE.name().toLowerCase(), EXPECTED_AGE);
		properties.store(Files.newOutputStream(tempProps.toPath()), null);
		System.setProperty(EnumWithPropertiesFile.SYSTEM_VARIABLE, tempProps.getAbsolutePath());
	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty(EnumWithPropertiesFile.SYSTEM_VARIABLE);
	}

	@Test
	public void shouldReadPropertiesFromFile() throws Exception {
		assertThat(name.get()).isEqualTo(EXPECTED_NAME);
		assertThat(age.get()).isEqualTo(EXPECTED_AGE);
	}

	@Deployment
	  public static JavaArchive createDeployment() {
	    return ShrinkWrap.create(JavaArchive.class)
	        .addClass(EnumWithPropertiesFile.class)
	        .addClass(EnumWithPropertiesFilePropertyFileConfigurationResolver.class)
	        .addClass(ConfigurationResolver.class)
	        .addClass(EnumWithPropertiesFileConfiguration.class)
	        .addClass(EnumWithPropertiesFileConfigurationBean.class)
	        .addAsManifestResource(
	            EmptyAsset.INSTANCE, "beans.xml");
	  }
}
