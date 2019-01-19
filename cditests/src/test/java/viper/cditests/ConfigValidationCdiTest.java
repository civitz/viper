package viper.cditests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import viper.ConfigurationResolver;

@RunWith(Arquillian.class)
public class ConfigValidationCdiTest {
  @Inject
  @MyConfig(ComplexEnum.AGE)
  Instance<Integer> age;

  @Test
  public void shouldInjectIntegers() throws Exception {
    assertThat(age.isAmbiguous())
      .as("Injection should be non ambiguous")
      .isFalse();
    assertThat(age.isUnsatisfied())
      .as("Injection should be satisfiable")
      .isFalse();
    assertThatThrownBy(()->  age.get())
      .as("Injection should fail for validation issues on NAME and AGE")
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(ComplexEnum.AGE.name())
      .hasMessageContaining(ComplexEnum.NAME.name());
  }

  @Deployment
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ComplexEnum.class)
        .addClass(ComplexEnumInvalidConfigResolver.class)
        .addClass(ConfigurationResolver.class)
        .addClass(MyConfig.class)
        .addClass(ComplexEnumConfInjector.class)
        .addAsManifestResource(
            EmptyAsset.INSTANCE, "beans.xml");
  }
}
