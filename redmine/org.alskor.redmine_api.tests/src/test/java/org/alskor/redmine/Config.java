package org.alskor.redmine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static final String TEST_PROPERTIES = "redmine_api_test.properties";

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
		return properties.getProperty("redmine.host");
	}

	public static String getUser() {
		return properties.getProperty("redmine.user");
	}

	public static String getApiKey() {
		return properties.getProperty("redmine.apikey");
	}

//	public static String getProjectKey() {
//		return properties.getProperty("redmine.projectKey");
//	}

//	public static Integer getQueryId() {
//		String s = properties.getProperty("redmine.queryId");
//		return Integer.parseInt(s);
//	}

	public static String getParam(String key) {
		return properties.getProperty(key);
	}
}
