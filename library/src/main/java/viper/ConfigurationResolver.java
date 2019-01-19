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
	 * Returns a string description of the key.
	 * <p>
	 * Use this if your key can be represented in a particular way. E.g. you are
	 * using a property file and all your configurations have a particular
	 * prefix. Or if you can provide a textual description of a key.
	 * <p>
	 * This method will be called if, for a particular key, the value is not
	 * valid: the string returned will be used to construct the error message,
	 * along with the enum constant name.
	 * 
	 * @param key
	 *            the configuration key.
	 * @return a string description of the key.
	 */
	default String getConfigurationKey(E key) {
		return key.name();
	}
}
