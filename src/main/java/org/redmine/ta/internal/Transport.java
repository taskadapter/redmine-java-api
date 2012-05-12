package org.redmine.ta.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.redmine.ta.RedmineException;
import org.redmine.ta.RedmineFormatException;
import org.redmine.ta.RedmineInternalError;
import org.redmine.ta.beans.Project;
import org.redmine.ta.internal.json.JsonFormatException;
import org.redmine.ta.internal.json.JsonObjectParser;
import org.redmine.ta.internal.json.JsonObjectWriter;

/**
 * Redmine transport utilities.
 * 
 * @author maxkar
 * 
 */
public final class Transport {
	private static final Map<Class<?>, EntityConfig<?>> OBJECT_CONFIGS = new HashMap<Class<?>, EntityConfig<?>>();
	private static final String FORMAT_SUFFIX = ".json";
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";

	static {
		OBJECT_CONFIGS.put(
				Project.class,
				config("project", "projects",
						RedmineJSONBuilder.CREATE_PROJECT_WRITER,
						RedmineJSONParser.PROJECT_PARSER));
	}


	/** Uri configurator */
	private final URIConfigurator configurator;
	private String login;
	private String password;
	private boolean useBasicAuth = false;


	public Transport(URIConfigurator configurator) {
		this.configurator = configurator;
	}

	/**
	 * Performs a post request.
	 * 
	 * @param object
	 *            object to use.
	 * @param params
	 *            name params.
	 * @return object to use.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <T> T post(T object, NameValuePair... params)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		URI uri = getURIConfigurator().createURI(
				config.multiObjectName + FORMAT_SUFFIX, params);
		HttpPost httpPost = new HttpPost(uri);
		String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName,
				object, config.writer);
		setEntity(httpPost, body);
		String response = getCommunicator().sendRequest(httpPost);
		return parseResponce(response, config.singleObjectName, config.parser);
	}

	private Communicator getCommunicator() {
		Communicator communicator = new Communicator();
		if (useBasicAuth) {
			communicator.setCredentials(login, password);
		}
		return communicator;
	}

	private static <T> T parseResponce(String responce, String tag,
			JsonObjectParser<T> parser) throws RedmineFormatException {
		try {
			return parser.parse(RedmineJSONParser.getResponceSingleObject(
					responce, tag));
		} catch (JsonFormatException e) {
			throw new RedmineFormatException(e);
		}
	}

	private void setEntity(HttpEntityEnclosingRequest request, String body) {
		StringEntity entity;
		try {
			entity = new StringEntity(body, Communicator.CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RedmineInternalError("Required charset "
					+ Communicator.CHARSET + " is not supported", e);
		}
		entity.setContentType(CONTENT_TYPE);
		request.setEntity(entity);
	}

	@SuppressWarnings("unchecked")
	private <T> EntityConfig<T> getConfig(Class<?> class1) {
		final EntityConfig<?> guess = OBJECT_CONFIGS.get(class1);
		if (guess == null)
			throw new RedmineInternalError("Unsupported class " + class1);
		return (EntityConfig<T>) guess;
	}

	private URIConfigurator getURIConfigurator() {
		return configurator;
	}

	private static <T> EntityConfig<T> config(String objectField,
			String urlPrefix, JsonObjectWriter<T> writer,
			JsonObjectParser<T> parser) {
		return new EntityConfig<T>(objectField, urlPrefix, writer, parser);
	}

	public void setCredentials(String login, String password) {
		this.login = login;
		this.password = password;
		this.useBasicAuth = true;
	}

	/**
	 * Entity config.
	 * 
	 * @author maxkar
	 * 
	 */
	static class EntityConfig<T> {
		final String singleObjectName;
		final String multiObjectName;
		final JsonObjectWriter<T> writer;
		final JsonObjectParser<T> parser;

		public EntityConfig(String objectField, String urlPrefix,
				JsonObjectWriter<T> writer, JsonObjectParser<T> parser) {
			super();
			this.singleObjectName = objectField;
			this.multiObjectName = urlPrefix;
			this.writer = writer;
			this.parser = parser;
		}

	}
}
