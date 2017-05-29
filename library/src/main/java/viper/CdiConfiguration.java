package viper;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Instruct the processor to generate a bean to inject and validate properties.
 * <p>
 * With no other annotations, supposing your enum is called <code>MyEnum</code>,
 * the processor will:
 * <ul>
 * <li>Generate a class named <code>MyEnumConfigurationBean</code></li>
 * <li>Generate a qualifier annotation named <code>MyEnumConfiguration</code>
 * parametrized on your enum</li>
 * <li>Provide no validation on properties' values</li>
 * <li>Provide a producer of type {@link String} with
 * <code>MyEnumConfiguration</code> qualifier</li>
 * </ul>
 *
 * <p>
 * With this annotation you can <code>@Inject</code> configuration on your beans
 * like this:
 *
 * <pre>
 * class MyClass {
 *  {@literal @}Inject
 *  {@literal @}MyEnumConfiguration(MyEnum.MY_ENUM_CONSTANT)
 *   String config;
 * }
 * </pre>
 * <p>
 * Use sub-annotations to obtain specific variants of the configuration bean:
 * <ul>
 * <li>{@link DefaultKey} to specify a different enum constant to use as default
 * key</li>
 * <li>{@link ConfigValidator} to specify a method of the enum which provides a
 * validator for the configuration</li>
 * <li>{@link PassAnnotations} to specify a set of annotations to be passed to
 * the generated configuration bean</li>
 * </ul>
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CdiConfiguration {

	/**
	 * If set to <code>true</code>, the configuration bean will have producers
	 * methods for primitives (Integer, Double, Long, Byte). The default is
	 * false.
	 */
	boolean producersForPrimitives() default false;

	/**
	 * Specifies an enum constant to be used as the default constant. This will
	 * be used to construct the qualifier annotation.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.FIELD)
	public static @interface DefaultKey {
	}

	/**
	 * Specifies a method to obtain a validator of the value of a property, in
	 * the form of a {@link Predicate} of type {@link String}.
	 * <p>
	 * If properties do not pass validation, the configuration bean will throw
	 * an exception upon initialization.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.METHOD })
	public static @interface ConfigValidator {
	}

	/**
	 * Specifies annotations to be passed to the configuration bean.
	 * <p>
	 * You may, for example, pass annotations such as
	 * <code>javax.enterprise.context.ApplicationScoped</code> to make the
	 * configuration bean last for the lifetime of the application.
	 * <p>
	 * The annotation accepts an array of annotations.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ ElementType.TYPE })
	public static @interface PassAnnotations {
		Class<? extends Annotation>[] value();
	}
}
