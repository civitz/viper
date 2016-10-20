package generatortests;

import java.util.function.Predicate;

import cdi.configure.ConfigurationKey;

@ConfigurationKey(propertiesPath="/opt/generatortests/config.properties")
public enum SomeEnum {
	@ConfigurationKey.KeyNullValue
	NULL,
	YO,
	MAMA;

	@ConfigurationKey.ConfigValidator
	public Predicate<String> getValidator(){
		return s -> true;
	}
	
	@ConfigurationKey.KeyString
	public String getKeyString() {
		return this.name().toLowerCase() + ".config";
	}
}
