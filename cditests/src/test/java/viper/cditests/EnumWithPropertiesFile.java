package viper.cditests;

import viper.PropertyFileResolver;

import viper.CdiConfiguration;

@CdiConfiguration
@PropertyFileResolver(propertiesPath="EnumWithPropertiesFile.properties")
public enum EnumWithPropertiesFile {
  NAME,
  AGE
}
