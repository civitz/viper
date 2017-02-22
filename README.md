# Viper

A generator and a framework for injecting configurations via CDI

## As simple as

```java

package tests;

import civitz.viper.CdiConfiguration;

@CdiConfiguration(propertiesPath="/opt/tests/config.properties")
public enum MyConfigs{
	NULL, // placeholder, will not be usable
	
	FIRST_PROPERTY,
	SECOND_PROPERTY;
}
```

Viper will generate a `ConfigurationBean` class, which will read properties from the specified path. If properties are missing, the bean will throw an `IllegalArgumentException` in the initialization method.

Viper will also generate a `Configuration` qualifier annotation, in this form:
```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
	@Nonbinding
	MyConfigs value() default MyConfigs.NULL;
}

```
Which permits you to inject configuration in this way:

```java

package tests.logic;

import javax.inject.Inject;
import tests.Configuration; // this is generated
import tests.MyConfigs;

public MyApplicationLogic {

	@Inject 
	@Configuration(MyConfigs.FIRST_PROPERTY)	
	String firstProperty;
	
	void myMethod(){
		// use the property, Luke
	}
}
```

## A more complex example

You can tune the generated bean with more annotations:

```java
package tests;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;

import civitz.viper.CdiConfiguration;

/*
 * You can specify one or more annotations for the Configuration Bean.
 * In this case, Configuration Bean will have the ApplicationScoped qualifier
 */
@CdiConfiguration.PassAnnotations({ApplicationScoped.class})
@CdiConfiguration(propertiesPath="/opt/generatortests/config.properties")
public enum CompleteEnum {

	FIRST_PROPERTY("my.particular.key", s -> Ints.tryParse(s) != null),
	SECOND_PROPERTY("my.other.key", s -> s.length() >= 10),

	/*
	 * You can mark a specific enum constant to be the null value instead of the
	 * first one
	 */
	@CdiConfiguration.KeyNullValue
	PLEASE_IGNORE_ME("");

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
	@CdiConfiguration.KeyString
	public String getKeyString() {
		return key;
	}
}

```