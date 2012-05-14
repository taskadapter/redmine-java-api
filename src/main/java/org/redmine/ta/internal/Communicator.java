package org.redmine.ta.internal;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.redmine.ta.*;
import org.redmine.ta.internal.json.JsonFormatException;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

public class Communicator {
    public static final String CHARSET = "UTF-8";

    private final Logger logger = LoggerFactory.getLogger(Communicator.class);
    private String login;
    private String password;

    // TODO lots of usages process 404 code themselves, but some don't.
    // check if we can process 404 code in this method instead of forcing clients to deal with it.

    /**
     * @return the response body
     */
    public String sendRequest(HttpRequest request) throws RedmineException {
        logger.debug(request.getRequestLine().toString());
        DefaultHttpClient httpclient = HttpUtil.getNewHttpClient();

        configureProxy(httpclient);

        if (login != null) {
            // replaced because of http://code.google.com/p/redmine-java-api/issues/detail?id=72
//			httpclient.getCredentialsProvider().setCredentials(
//                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
//                new UsernamePasswordCredentials(login, password));
            String credentials;
            try {
				credentials = "\""
						+ Base64.encodeBase64String(
								(login + ':' + password).getBytes(CHARSET))
								.trim() + "\"";
            } catch (UnsupportedEncodingException e) {
                throw new RedmineInternalError(e);
            }
            request.addHeader("Authorization", "Basic: " + credentials);
        }

        request.addHeader("Accept-Encoding", "gzip,deflate");
        HttpResponse httpResponse;
        try {
            httpResponse = httpclient.execute((HttpUriRequest) request);
        } catch (ClientProtocolException e1) {
            throw new RedmineFormatException(e1);
        } catch (IOException e1) {
            throw new RedmineTransportException(e1);
        }

        int responseCode = httpResponse.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new RedmineAuthenticationException("Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server.");
        }
        if (responseCode == HttpStatus.SC_FORBIDDEN) {
            throw new NotAuthorizedException("Forbidden. Please check the user has proper permissions.");
        }

        HttpEntity responseEntity = httpResponse.getEntity();
        String responseBody;
        try {
            responseBody = EntityUtils.toString(responseEntity);
        } catch (ParseException e) {
            throw new RedmineFormatException(e);
        } catch (IOException e) {
            throw new RedmineTransportException(e);
        }

        if (responseCode == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException("Server returned '404 not found'. response body:" + responseBody);
        }

        if (responseCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			List<String> errors;
			try {
				errors = RedmineJSONParser.parseErrors(responseBody);
			} catch (JsonFormatException e) {
				throw new RedmineFormatException("Bad redmine error responce",
						e);
			}
            throw new RedmineProcessingException(errors);
        }
        /* 422 "invalid"
          <?xml version="1.0" encoding="UTF-8"?>
          <errors>
            <error>Name can't be blank</error>
            <error>Identifier has already been taken</error>
          </errors>
           */
        httpclient.getConnectionManager().shutdown();
        return responseBody;
    }

    private void configureProxy(DefaultHttpClient httpclient) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null && proxyPort != null) {
            int port;
            try {
                port = Integer.parseInt(proxyPort);
            } catch (NumberFormatException e) {
                throw new RedmineConfigurationException("Illegal proxy port " + proxyPort, e);
            }
            HttpHost proxy = new HttpHost(proxyHost, port);
            httpclient.getParams().setParameter(org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY, proxy);
            String proxyUser = System.getProperty("http.proxyUser");
            if (proxyUser != null) {
                String proxyPassword = System.getProperty("http.proxyPassword");
                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope(proxyHost, port),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword));
            }
        }
    }

    public void setCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String sendGet(URI uri) throws RedmineException {
        HttpGet http = new HttpGet(uri);
        return sendRequest(http);
    }
}
