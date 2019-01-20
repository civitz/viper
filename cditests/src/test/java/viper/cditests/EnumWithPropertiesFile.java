package viper.cditests;

import viper.PropertyFileResolver;

import viper.CdiConfiguration;

@CdiConfiguration
@PropertyFileResolver(propertiesPath = "EnumWithPropertiesFile.properties", systemPropertyName = EnumWithPropertiesFile.SYSTEM_VARIABLE)
public enum EnumWithPropertiesFile {
	NAME,
	AGE;

	public static final String SYSTEM_VARIABLE = "customPath";
}
