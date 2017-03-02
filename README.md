# Viper

[![Join the chat at https://gitter.im/cdi-viper/Lobby](https://badges.gitter.im/cdi-viper/Lobby.svg)](https://gitter.im/cdi-viper/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A generator and a framework for injecting configurations via CDI.

Put all your configuration keys in an enum, and make viper inject the configurations in your beans.

## As simple as

Import library and generators in maven:

```xml
<dependency>
	<groupId>civitz.viper</groupId>
	<artifactId>library</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>civitz.viper</groupId>
	<artifactId>generator</artifactId>
	<version>0.1.0-SNAPSHOT</version>
	<scope>provided</scope>
	<optional>true</optional>
</dependency>

```

Then annotate the enum of your configuration keys as follows:

```java

package tests;

import civitz.viper.CdiConfiguration;

@CdiConfiguration
@PropertyFileResolver(propertiesPath = "/tmp/viper/my.config")
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
@Target({FIELD,TYPE,METHOD,PARAMETER})
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

import com.google.common.primitives.Ints;

import viper.CdiConfiguration;
import viper.PropertyFileResolver;

/*
 * You can specify one or more annotations for the Configuration Bean.
 * In this case, Configuration Bean will have the ApplicationScoped qualifier
 */
@CdiConfiguration.PassAnnotations(ApplicationScoped.class)
@CdiConfiguration
@PropertyFileResolver(propertiesPath = "/tmp/viper/my.config")
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
	@PropertyFileResolver.KeyString
	public String getKeyString() {
		return key;
	}
}

```

Or you can omit the `@PropertyFileResolver` and provide your own @Inject-able implementation of `ConfigurationResolver<YourEnum>` via CDI. For example, you can create a configuration resolver for etcd.  