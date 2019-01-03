package viper.cditests;

import viper.ConfigurationResolver;

public class FakeSimpleEnumConfigResolver implements ConfigurationResolver<SimplestEnum> {

  @Override
  public String getConfigurationValue(SimplestEnum key) {
    switch (key) {
    case LOCATION:
      return "Earth";
    case NAME:
      return "Obama";
    default:
      throw new IllegalArgumentException("No conf value for " + key);
    }
  }

}