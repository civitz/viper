package viper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instruct the processor to generate a bean to read properties from a file.
 * <p>
 * The file is specified in the <code>propertiesPath</code> property, and must
 * point to a valid file at runtime.
 * <p>
 * By default it uses <code>enumConstant.name().toLowerCase()</code> as a
 * property key.
 * <p>
 * Alternative paths can be specified at run time by passing a system property
 * with -Dproperty=path. Property name can be customized with
 * {@link #systemPropertyName()}, the default is  <code>{enum class}ConfigPath</code>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PropertyFileResolver {

	/**
	 * The path where the properties are stored.
	 */
	String propertiesPath();

	/**
	 * Set the system property name for alternative property path. Use `*` to
	 * automatically place the enum class name.
	 */
	String systemPropertyName() default "*ConfigPath";

	/**
	 * Specifies a method to obtain the key in place of
	 * <code>enumConstant.name().toLowerCase()</code>
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.METHOD })
	public static @interface KeyString {
	}

}
