package viper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PropertyFileResolver {
	String propertiesPath();
	

	/**
	 * Specifies a method to obtain the key in place of
	 * <code>enumConstant.name().toLowerCase()</code>
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.METHOD })
	public static @interface KeyString {
	}

}
