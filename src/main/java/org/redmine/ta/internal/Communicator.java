package org.redmine.ta.internal;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.castor.core.util.Base64Encoder;
import org.redmine.ta.AuthenticationException;
import org.redmine.ta.NotFoundException;
import org.redmine.ta.RedmineException;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Communicator {
    public static final String CHARSET = "UTF-8";

    private Logger logger = LoggerFactory.getLogger(Communicator.class);
    private String login;
    private String password;

    // TODO lots of usages process 404 code themselves, but some don't.
    // check if we can process 404 code in this method instead of forcing clients to deal with it.
    public Response sendRequest(HttpRequest request) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        logger.debug(request.getRequestLine().toString());
        DefaultHttpClient httpclient = HttpUtil.getNewHttpClient();

        configureProxy(httpclient);

        if (login != null) {
            // replaced because of http://code.google.com/p/redmine-java-api/issues/detail?id=72
//			httpclient.getCredentialsProvider().setCredentials(
//                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
//                new UsernamePasswordCredentials(login, password));
            final String credentials = String.valueOf(Base64Encoder.encode((login + ':' + password).getBytes(CHARSET)));
            request.addHeader("Authorization", "Basic: " + credentials);
        }

        request.addHeader("Accept-Encoding", "gzip,deflate");
        HttpResponse httpResponse = httpclient.execute((HttpUriRequest) request);

        int responseCode = httpResponse.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthenticationException("Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server.");
        }
        if (responseCode == HttpStatus.SC_FORBIDDEN) {
            throw new AuthenticationException("Forbidden. Please check the user has proper permissions.");
        }

        HttpEntity responseEntity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(responseEntity);

        if (responseCode == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException("Server returned '404 not found'. response body:" + responseBody);
        }

        if (responseCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
            List<String> errors = RedmineXMLParser.parseErrors(responseBody);
            throw new RedmineException(errors);
        }
        /* 422 "invalid"
          <?xml version="1.0" encoding="UTF-8"?>
          <errors>
            <error>Name can't be blank</error>
            <error>Identifier has already been taken</error>
          </errors>
           */


        // have to fill our own object, otherwise there's no guarantee
        // that the request body can be retrieved later ("socket closed" exception can occur)
        Response r = new Response(responseCode, responseBody);
        httpclient.getConnectionManager().shutdown();
        return r;
    }

    private void configureProxy(DefaultHttpClient httpclient) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null && proxyPort != null) {
            int port = Integer.parseInt(proxyPort);
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
}
