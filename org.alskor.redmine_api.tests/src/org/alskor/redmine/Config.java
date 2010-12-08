package org.alskor.redmine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static final String TEST_PROPERTIES = "hostedreadmine.properties";
//	private static final String TEST_PROPERTIES = "linuxvbox.properties";
//	private static final String TEST_PROPERTIES = "vbox-home.properties";

	private static Properties properties = new Properties();
	static {

		InputStream is = Config.class.getClassLoader().getResourceAsStream(
				TEST_PROPERTIES);
		try {
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getHost() {
		return properties.getProperty("redmine.host");
	}

	public static String getUser() {
		return properties.getProperty("redmine.user");
	}

	public static String getApiKey() {
		return properties.getProperty("redmine.apikey");
	}

	public static String getProjectKey() {
		return properties.getProperty("redmine.projectKey");
	}

	public static String getQueryId() {
		return properties.getProperty("redmine.queryId");
	}
	
	public static String getParam(String key) {
		return properties.getProperty(key);
	}
}
