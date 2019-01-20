package viper.cditests;

import viper.CdiConfiguration;
import viper.PropertyFileResolver;

@CdiConfiguration(producersForPrimitives=true)
@PropertyFileResolver(propertiesPath="NoCdiEnum.properties")
public enum NoCdiEnum {
	NAME,
	AGE
}
