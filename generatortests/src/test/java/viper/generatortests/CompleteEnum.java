package viper.generatortests;

import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;

import com.google.common.primitives.Ints;

import viper.CdiConfiguration;
import viper.PropertyFileResolver;

/*
 * You can specify one or more annotations for the Configuration Bean.
 * In this case, Configuration Bean will have the ApplicationScoped qualifier
 */
@CdiConfiguration.PassAnnotations(ApplicationScoped.class)
/*
 * Tell the processor to create producers also for primitive types: Byte,
 * Character, Short, Integer, Long, Float, Double, Boolean. Take this feature
 * with care: we don't verify if the transformation from string is possible,
 * exceptions may be thrown.
 * 
 * You can also specify the name of the generated annotation and configuration
 * bean. You can put a star symbol in it, and the processor will replace it with
 * the enum name.
 */
@CdiConfiguration(producersForPrimitives = true, annotationName = "MyConfig", configurationBeanName = "*ConfInjector")
/*
 * Generate a Properties-based file-sourced configuration resolver with the
 * given configuration file's path
 */
@PropertyFileResolver(propertiesPath = "/tmp/viper/my.config")
public enum CompleteEnum {

	FIRST_PROPERTY("my.particular.key", s -> Ints.tryParse(s) != null),

	/*
	 * You can mark a specific enum constant to be the default value instead of the
	 * first one
	 */
	@CdiConfiguration.DefaultKey
	SECOND_PROPERTY("my.other.key", s -> s.length() >= 10);

	String key;
	Predicate<String> validator;

	CompleteEnum(String key) {
		this(key, s -> true);
	}

	CompleteEnum(String key, Predicate<String> validator) {
		this.key = key;
		this.validator = validator;
	}

	/*
	 * You can specify a method to get a validator for a property: if the
	 * property is not valid, the Configuration Bean will throw an
	 * IllegalArgumentException in the initialization method.
	 */
	@CdiConfiguration.ConfigValidator
	public Predicate<String> getValidator() {
		return validator;
	}

	/*
	 * You can specify a method to get a particular string value as a property
	 * key instead of the generic enum constant name in lowercase.
	 */
	@PropertyFileResolver.KeyString
	public String getKeyString() {
		return key;
	}
}
