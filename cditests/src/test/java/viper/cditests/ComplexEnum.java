package viper.cditests;

import com.google.common.primitives.Ints;
import java.util.function.Predicate;
import javax.enterprise.context.ApplicationScoped;
import viper.CdiConfiguration;

@CdiConfiguration.PassAnnotations(ApplicationScoped.class)
@CdiConfiguration(producersForPrimitives = true, annotationName = "MyConfig", configurationBeanName = "*ConfInjector")
public enum ComplexEnum {
  NAME(s -> s != null && !s.isEmpty()),
  AGE(s -> s != null && Ints.tryParse(s) != null);

  Predicate<String> validator;

  private ComplexEnum(Predicate<String> validator) {
    this.validator = validator;
  }

  @CdiConfiguration.ConfigValidator
  Predicate<String> getValidator() {
    return validator;
  }
}
