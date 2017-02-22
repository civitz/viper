package generatortests;

import java.util.function.Predicate;

import civitz.viper.CdiConfiguration;

@CdiConfiguration(propertiesPath="/opt/generatortests/config.properties")
public enum SomeEnum {
	@CdiConfiguration.KeyNullValue
	NULL,
	YO,
	MAMA;

	@CdiConfiguration.ConfigValidator
	public Predicate<String> getValidator(){
		return s -> true;
	}
	
	@CdiConfiguration.KeyString
	public String getKeyString() {
		return this.name().toLowerCase() + ".config";
	}
}
