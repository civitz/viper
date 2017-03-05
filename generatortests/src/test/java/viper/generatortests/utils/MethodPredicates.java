package viper.generatortests.utils;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MethodPredicates {
	public static Predicate<Method> isStatic() {
		return m -> (m.getModifiers() & Modifier.STATIC) != 0;
	}
	
	public static Predicate<Method> hasName(String name) {
		return m -> m.getName().equals(name);
	}
	
	public static Predicate<Method> isVoid() {
		return m -> m.getReturnType().equals(Void.TYPE);
	}
	
	public static Predicate<Method> hasReturnType(Type type) {
		return m -> m.getReturnType().equals(type);
	}
	
	public static Predicate<Method> hasParametersOfType(Class<?>... types) {
		return m -> Arrays.equals(m.getParameterTypes(), types);
	}
	
	@SafeVarargs
	public static Predicate<Method> hasAnnotations(Class<? extends Annotation> annotation, Class<? extends Annotation>... other) {
		return m -> {
			List<?> list = Arrays.stream(m.getAnnotations())
					.map(Annotation::annotationType)
					.collect(toList());

			return list.contains(annotation) && list.containsAll(Arrays.asList(other));
		};
	}
}
