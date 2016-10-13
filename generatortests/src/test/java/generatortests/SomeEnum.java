package generatortests;

import cdi.configure.ConfigurationKey;

@ConfigurationKey(propertiesPath="/opt/generatortests/config.properties")
public enum SomeEnum {
	NULL,
	YO,

	MAMA;
	
}
