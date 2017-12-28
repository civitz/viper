# Viper

[![Join the chat at https://gitter.im/cdi-viper/Lobby](https://badges.gitter.im/cdi-viper/Lobby.svg)](https://gitter.im/cdi-viper/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![Build status](https://api.travis-ci.org/civitz/viper.svg)

A generator and a framework for injecting configurations via the java EE's CDI.

Put all your configuration keys in an enum, and make viper inject the configurations in your beans.

## Why another configuration library?

There are many configuration frameworks around, and everyone is good in its own way. For those which support CDI-based configuration injection, the magic is usually done via annotation from the framework.

What happens with framework-provided annotations is:
- you can not trace back which configuration you need until you actually create an instance of an object and thus trigger configuration injection
- each object is aware of the configuration keys it actually needs from the configuration source (e.g. the key in a configuration file)
- injection happens necessarily with String-based keys

What we want is a single source to enumerate the available configuration keys an application may need. By doing this we can:

- centralize the source and know in advance which configurations an application will need
- place validation criterias alongside configuration keys
- make sure the inject-able configurations come only from the pool of known configurations

We found a way to do this by using enums to limit the configuration keys, and generating the necessary annotations and configuration-injection code.

Want to try this?

## As simple as

Import library and generators in maven:

```xml
<dependency>
	<groupId>com.github.civitz.viper</groupId>
	<artifactId>library</artifactId>
	<version>0.2.0</version>
</dependency>
<dependency>
	<groupId>com.github.civitz.viper</groupId>
	<artifactId>generator</artifactId>
	<version>0.2.0</version>
	<scope>provided</scope>
	<optional>true</optional>
</dependency>

```

You can also skip the generator dependency if you use `maven-compiler-plugin` version 3.5+ by using this plugin configuration:
```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<useIncrementalCompilation>false</useIncrementalCompilation>
		<annotationProcessorPaths>
			<annotationProcessorPath>
				<groupId>com.github.civitz.viper</groupId>
				<artifactId>generator</artifactId>
				<version>0.2.0</version>
			</annotationProcessorPath>
		</annotationProcessorPaths>
	</configuration>
</plugin>
```

Be also sure to have the [Contexts and Dependency Injection for Java EE (CDI)](http://cdi-spec.org/) API as a dependency.

Then annotate the enum of your configuration keys as follows:

```java

package tests;

import civitz.viper.CdiConfiguration;

@CdiConfiguration
@PropertyFileResolver(propertiesPath = "/tmp/viper/my.config")
public enum MyConfigs {	
	FIRST_PROPERTY,
	SECOND_PROPERTY;
}
```

Viper will generate a `MyConfigsPropertyFileConfigurationResolver` class, which will read properties from the specified path: by default it will assume your properties are `Properties`-compatible, and use `key.name().toLowerCase()` as key.

It will create a `MyConfigsConfiguration` qualifier annotation, in this form:
```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,TYPE,METHOD,PARAMETER})
public @interface MyConfigsConfiguration {
	@Nonbinding
	MyConfigs value() default MyConfigs.FIRST_PROPERTY;
}

```
It will also generate a `MyConfigsConfigurationBean` class, which will inject your `MyConfigsConfiguration` annotated configuration.

You can use the generated code in this way:

```java

package tests.logic;

import javax.inject.Inject;
import tests.MyConfigsConfiguration; // this is generated
import tests.MyConfigs;

public MyApplicationLogic {

	@Inject 
	@MyConfigsConfiguration(MyConfigs.FIRST_PROPERTY)
	String firstProperty;
	
	@Inject
	@MyConfigsConfiguration(MyConfigs.SECOND_PROPERTY)
	String secondProperty;

	void myMethod() {
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
	 * You can mark a specific enum constant to be the default key instead of the
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

```

## A custom configuration resolver

You can omit the `@PropertyFileResolver` and provide your own @Inject-able implementation of `ConfigurationResolver<YourEnum>` via CDI. For example, supposing you have this enum:

```java
package tests;

import civitz.viper.CdiConfiguration;

@CdiConfiguration
public enum MyConfigs {	
	FIRST_PROPERTY,
	SECOND_PROPERTY;
}
```

You can write your own:

```java
package test;

import viper.ConfigurationResolver;

public class OuterSpaceConfigurationResolver implements ConfigurationResolver<MyConfigs> {

	@Override
	public String getConfigurationValue(MyConfigs key) {
		// you can extract data from the enum here, since you know it
		return fetchConfigurationValueFromOuterSpace(key);
	}
	
	/*
	 * You can optionally override getConfigurationKey to provide a meaningful
	 * string representation of your configuration key.
	 */
	@Override
	public String getConfigurationKey(MyConfigs key) {
		return "my.conf.prefix."+key.name().toLowerCase();
	}

	// implementation for fetchConfigurationValueFromOuterSpace omitted
}
```

And the generated `MyConfigsConfigurationBean` will use that class (via CDI, so be sure it's inject-able) to resolve properties.

Be aware of the fact that you can't inject `MyConfigs` properties in your own resolver, since it would create a circular injection dependency. Use another method to configure your resolver (e.g. read properties from file, or from `System`).

## Suggested use

We suggest to:

- provide a validator and at least check for null values if you do not want them (by default we do not check)
- if you build a custom resolver please implement the `getConfigurationKey` method and provide a textual description for when configuration values are invalid
- pass at least `javax.enterprise.context.ApplicationScoped` annotation to the `ConfigurationBean` to avoid multiple validation of properties.

## Contribute

If you find a bug or want to discuss new features, please file an issue first ;)
