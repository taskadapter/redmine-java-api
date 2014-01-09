package com.taskadapter.redmineapi.internal.comm.redmine;

import com.taskadapter.redmineapi.NotAuthorizedException;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineProcessingException;
import com.taskadapter.redmineapi.internal.RedmineJSONParser;
import com.taskadapter.redmineapi.internal.comm.BasicHttpResponse;
import com.taskadapter.redmineapi.internal.comm.Communicators;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;
import org.apache.http.HttpStatus;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RedmineErrorHandler implements
        ContentHandler<BasicHttpResponse, BasicHttpResponse> {

	private static final Map<String, String> ERROR_REMAP = new HashMap<String, String>();

	static {
		ERROR_REMAP
				.put("Priority can't be blank",
						"Priority can't be blank. No default priority is set in the Redmine server settings. please use menu \"Administration -> Enumerations -> Issue Priorities\" to set the default priority.");
	}

	@Override
	public BasicHttpResponse processContent(BasicHttpResponse httpResponse)
			throws RedmineException {
		final int responseCode = httpResponse.getResponseCode();
		if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
			throw new RedmineAuthenticationException(
					"Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server.");
		}
		if (responseCode == HttpStatus.SC_FORBIDDEN) {
			throw new NotAuthorizedException(
					"Forbidden. Please check the user has proper permissions.");
		}
		if (responseCode == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException(
					"Server returned '404 not found'. response body:"
							+ getContent(httpResponse));
		}

		if (responseCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			List<String> errors;
			try {
				errors = RedmineJSONParser.parseErrors(getContent(httpResponse));
				errors = remap(errors);
			} catch (JSONException e) {
				throw new RedmineFormatException("Bad redmine error response", e);
			}
			throw new RedmineProcessingException(errors);
		}
		return httpResponse;
	}

	private List<String> remap(List<String> errors) {
		final List<String> result = new ArrayList<String>(errors.size());
		for (String message : errors)
			result.add(remap(message));
		return result;
	}

	private String remap(String message) {
		final String guess = ERROR_REMAP.get(message);
		return guess != null ? guess : message;
	}

	private String getContent(BasicHttpResponse entity) throws RedmineException {
		return Communicators.contentReader().processContent(entity);
	}

}
