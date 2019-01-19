package viper.cditests;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import viper.ConfigurationResolver;

@RunWith(Arquillian.class)
public class EnumWithPropertiesFileTest {

  private static final String EXPECTED_AGE = "42";
  private static final String EXPECTED_NAME = "Obama";

  @Inject
  @EnumWithPropertiesFileConfiguration(EnumWithPropertiesFile.NAME)
  Instance<String> name;

  @Inject
  @EnumWithPropertiesFileConfiguration(EnumWithPropertiesFile.AGE)
  Instance<String> age;

  static Path propertiesPath;

  @BeforeClass
  public static void setUp() throws Exception{
    /* Generating property file from path encoded in generated configuration resolver.
     * This is ugly but the alternative is to
     *  + edit the enum by manipulating the java source file and changing the property file's path
     *  + re-run the compiler via java
     *  + class-load the compiled sources and generated sources
     *  + generate a deployable jar with shrinkwrap
     *  + make sure there is a valid property file in the path
     *  + finally run the test
     */
    Field field = EnumWithPropertiesFilePropertyFileConfigurationResolver.class.getDeclaredField(
        "PROPERTIES_PATH");
    field.setAccessible(true);
    String propertyFilePath = String.class.cast(field.get(null));

    Properties properties = new Properties();
    properties.setProperty(EnumWithPropertiesFile.NAME.name().toLowerCase(), EXPECTED_NAME);
    properties.setProperty(EnumWithPropertiesFile.AGE.name().toLowerCase(), EXPECTED_AGE);
    propertiesPath = Paths.get(propertyFilePath);
    Files.deleteIfExists(propertiesPath);
    properties.store(Files.newOutputStream(propertiesPath), null);
  }

  @AfterClass
  public static void cleanup() throws Exception {
	  Files.deleteIfExists(propertiesPath);
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
