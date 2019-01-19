package viper.cditests;

import viper.ConfigurationResolver;

public class FakeComplexEnumConfigResolver implements ConfigurationResolver<ComplexEnum> {
  @Override
  public String getConfigurationValue(ComplexEnum key) {
    switch (key) {
    case AGE:
      return "42";
    case NAME:
      return "Obama";
    default:
      throw new IllegalArgumentException("No config for key " + key);

    }
  }
}
