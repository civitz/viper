package cdi.configure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ConfigurationKey {

	String propertiesPath();

	@Retention(RetentionPolicy.CLASS)
	@Target({ ElementType.METHOD })
	public static @interface KeyString {
	}

	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.FIELD)
	public static @interface KeyNullValue {
	}
	
	@Retention(RetentionPolicy.CLASS)
	@Target({ ElementType.METHOD })
	public static @interface ConfigValidator {
	}
}
