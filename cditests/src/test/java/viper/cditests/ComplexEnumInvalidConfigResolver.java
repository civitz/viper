package viper.cditests;

import viper.ConfigurationResolver;

/**
 * This configuration resolver purposely returns invalid values for all configuration keys.
 */
public class ComplexEnumInvalidConfigResolver implements ConfigurationResolver<ComplexEnum> {
  @Override
  public String getConfigurationValue(ComplexEnum key) {
    switch (key) {
    case AGE:
      return "i am a wrong value";
    case NAME:
      return ""; // wrong value too
    default:
      throw new IllegalArgumentException("No config for key " + key);
    }
  }
}