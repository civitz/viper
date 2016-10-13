package cdi.configure;

public @interface ConfigurationKey {
	
	String propertiesPath();
	
	public static @interface KeyString {
	}
	
	public static @interface KeyNullValue {
	}
}
