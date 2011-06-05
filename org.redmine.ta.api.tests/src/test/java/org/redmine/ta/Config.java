package org.redmine.ta;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static final String TEST_PROPERTIES = "api_test.properties";

	private static Properties properties = new Properties();
	static {

		InputStream is = Config.class.getClassLoader().getResourceAsStream(
				TEST_PROPERTIES);
		if(is == null) {
			throw new RuntimeException("Can't find file " + TEST_PROPERTIES +
					" in classpath. Please create it using one of the templates");
		}
		try {
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getHost() {
		return properties.getProperty("host");
	}

	public static String getLogin() {
		return properties.getProperty("user");
	}

	public static String getPassword() {
		return properties.getProperty("password");
	}

	public static String getApiKey() {
		return properties.getProperty("apikey");
	}

	public static String getParam(String key) {
		return properties.getProperty(key);
	}
}
