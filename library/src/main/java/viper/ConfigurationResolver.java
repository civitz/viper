package viper;

/**
 * An interface to resolve configuration values from an enum representing its
 * keys.
 *
 * @param <E>
 *            The enum containing the keys
 */
public interface ConfigurationResolver<E extends Enum<E>> {

	/**
	 * Returns a string value of the configuration key passed as a parameter.
	 * 
	 * @param key
	 *            the configuration key.
	 * @return the configuration value.
	 */
	String getConfigurationValue(E key);

	/**
	 * Returns a string representation of the key.
	 * <p>
	 * Use this if your key can be represented in a particular way. E.g. you are
	 * using a property file and all your configurations have a particular
	 * prefix.
	 * <p>
	 * This method will be called if, for a particular key, no value is found,
	 * or the value is not valid: the string returned will be used to construct
	 * the error message, along with the enum key name.
	 * 
	 * @param key
	 *            the configuration key.
	 * @return a string representation of the key.
	 */
	default String getConfigurationKey(E key) {
		return key.name();
	};
}
