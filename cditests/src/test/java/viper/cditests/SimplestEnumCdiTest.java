package viper.cditests;

import org.junit.Test;
import org.junit.runner.RunWith;
import viper.ConfigurationResolver;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@RunWith(Arquillian.class)
public class SimplestEnumCdiTest {

  @Inject
  @SimplestEnumConfiguration(SimplestEnum.LOCATION)
  Instance<String> location;

  @Inject
  Instance<SimplestEnumConfigurationBean> injector;

  @Test
  public void shouldInjectStrings() throws Exception {
    assertThat(location.isAmbiguous()).isFalse();
    assertThat(location.isUnsatisfied()).isFalse();
    assertThat(location.get()).isEqualTo("Earth");
  }

  @Test
  public void shouldBeAbletoInjectConfigurationBeanDirectly() throws Exception {
    assertThat(injector.isAmbiguous()).isFalse();
    assertThat(injector.isUnsatisfied()).isFalse();
  }

  @Deployment
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class)
        .addClass(SimplestEnum.class)
        .addClass(FakeSimpleEnumConfigResolver.class)
        .addClass(ConfigurationResolver.class)
        .addClass(SimplestEnumConfiguration.class)
        .addClass(SimplestEnumConfigurationBean.class)
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

}
