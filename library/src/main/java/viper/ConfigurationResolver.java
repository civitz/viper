package viper;

public interface ConfigurationResolver<E extends Enum<E>> {
	String getConfigurationValue(E key);
	
	String getConfigurationKey(E key);
}
