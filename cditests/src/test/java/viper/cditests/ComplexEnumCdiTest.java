package viper.cditests;

import static org.assertj.core.api.Assertions.assertThat;

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
public class ComplexEnumCdiTest {
  @Inject
  @MyConfig(ComplexEnum.AGE)
  Instance<Integer> age;

  @Inject
  @MyConfig(ComplexEnum.NAME)
  Instance<String> name;

  @Inject
  Instance<ComplexEnumConfInjector> injector;

  @Test
  public void shouldInjectIntegers() throws Exception {
    assertThat(age.isAmbiguous()).isFalse();
    assertThat(age.isUnsatisfied()).isFalse();
    assertThat(age.get()).isEqualTo(42);
  }

  @Test
  public void shouldInjectStrings() throws Exception {
    assertThat(name.isAmbiguous()).isFalse();
    assertThat(name.isUnsatisfied()).isFalse();
    assertThat(name.get()).isEqualTo("Obama");
  }

  @Test
  public void shouldBeAbletoInjectConfigurationBeanDirectly() throws Exception {
    assertThat(injector.isAmbiguous()).isFalse();
    assertThat(injector.isUnsatisfied()).isFalse();
  }

  @Deployment
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(ComplexEnum.class)
        .addClass(FakeComplexEnumConfigResolver.class)
        .addClass(ConfigurationResolver.class)
        .addClass(MyConfig.class)
        .addClass(ComplexEnumConfInjector.class)
        .addAsManifestResource(
            EmptyAsset.INSTANCE, "beans.xml");
  }
}
