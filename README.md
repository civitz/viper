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
		// use the property
	}
}
```