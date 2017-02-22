package civitz.viper;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CdiConfiguration {

	String propertiesPath();

	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.METHOD })
	public static @interface KeyString {
	}

	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.FIELD)
	public static @interface KeyNullValue {
	}
	
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.METHOD })
	public static @interface ConfigValidator {
	}
	
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.TYPE })
	public static @interface PassAnnotations {
		Class<? extends Annotation>[] value();
	}
}
