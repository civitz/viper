package generatortests;

import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;

import civitz.viper.CdiConfiguration;

@CdiConfiguration(propertiesPath="/opt/generatortests/config.properties")
@CdiConfiguration.PassAnnotations(ApplicationScoped.class)
public enum CompleteEnum {
	@CdiConfiguration.KeyNullValue
	NULL,
	FIRST_PROPERTY,
	SECOND_PROPERTY;

	@CdiConfiguration.ConfigValidator
	public Predicate<String> getValidator(){
		return s -> true;
	}
	
	@CdiConfiguration.KeyString
	public String getKeyString() {
		return this.name().toLowerCase() + ".config";
	}
}
