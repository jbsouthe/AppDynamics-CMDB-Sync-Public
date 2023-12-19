package com.appdynamics.cmdb.authentication;

import com.appdynamics.exceptions.CMDBBadStatusException;
import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.config.Configuration;
import com.appdynamics.util.HttpClientFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class ApigeeOAuthClient implements IOAuthClient {
    private static final Logger logger = LogManager.getFormatterLogger();
    private String oauthurl;
    private ArrayList<NameValuePair> authParameters = new ArrayList<>();
    private OAuthToken oAuthToken = null;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        private String uri = "Unset";
        public void setUri( String uri ) { this.uri=uri; }

        @Override
        public String handleResponse( final HttpResponse response) throws IOException {
            final int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_TEMPORARY_REDIRECT) {
                final HttpEntity entity = response.getEntity();
                try {
                    return entity != null ? EntityUtils.toString(entity) : null;
                } catch (final ParseException ex) {
                    throw new ClientProtocolException(ex);
                }
            } else {
                throw new CMDBBadStatusException(response.getStatusLine().toString(), EntityUtils.toString(response.getEntity()), uri);
            }
        }

    };

    public ApigeeOAuthClient (String oauthurl, String client_id, String client_secret, String client_scope, Configuration configuration) {
        this.oauthurl = oauthurl;
        this.authParameters.add( new BasicNameValuePair("client_id", client_id));
        this.authParameters.add( new BasicNameValuePair("client_secret", client_secret));
        this.authParameters.add( new BasicNameValuePair("scope", client_scope));
        this.authParameters.add( new BasicNameValuePair("grant_type","client_credentials"));
    }

    public String getAccessToken () throws CMDBException {
        if (this.oAuthToken == null || this.oAuthToken.isExpired())
            this.oAuthToken = getNewOAuthToken();
        return oAuthToken.access_token;
    }

    private OAuthToken getNewOAuthToken () throws CMDBException {
        try {
            URL url = new URL(oauthurl);
            HttpClient oauthClient = HttpClientFactory.getHttpClient(url.getHost());
            HttpPost request = new HttpPost(url.toURI());
            addPostParameters(request, authParameters);
            logger.trace("Request to run: %s",request.toString());
            for( Header header : request.getAllHeaders())
                logger.trace("with header: %s",header.toString());
            HttpResponse response = null;
            int tries=0;
            boolean succeeded=false;
            while( !succeeded && tries < 3 ) {
                try {
                    response = oauthClient.execute(request);
                    succeeded=true;
                    logger.trace("Response Status Line: %s", response.getStatusLine());
                } catch (IOException e) {
                    logger.error("Exception in attempting to get access token, Exception: %s", e.getMessage());
                    tries++;
                } catch (IllegalStateException illegalStateException) {
                    tries++;
                    oauthClient = HttpClientFactory.getHttpClient(url.getHost(),true);
                    logger.warn("Caught exception on connection, building a new connection for retry, Exception: %s", illegalStateException.getMessage());
                }
            }
            if( !succeeded ) throw new CMDBException("CMDB Requests Failed after many retries");
            HttpEntity entity = response.getEntity();
            Header encodingHeader = entity.getContentEncoding();
            Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 : Charsets.toCharset(encodingHeader.getValue());
            String json = null;
            try {
                json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                logger.trace("JSON returned: %s",json);
            } catch (IOException e) {
                logger.warn("IOException parsing returned encoded string to json text: "+ e.getMessage());
                throw new CMDBException(String.format("IOException parsing returned encoded string to json text: "+ e.getMessage()));
            }
            if( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.warn("Access Key retrieval returned bad status: %s message: %s", response.getStatusLine(), json);
                throw new CMDBException(String.format("Access Key retrieval returned bad status: %s message: %s", response.getStatusLine(), json));
            }
            OAuthToken token = gson.fromJson(json, OAuthToken.class);
            return token;
        } catch (Exception e) {
            logger.debug("IO Exception attempting to retrieve a token", e);
            throw new CMDBException(e.getMessage());
        }
    }

    private void addPostParameters (HttpPost request, ArrayList<NameValuePair> parameters) throws CMDBException {
        try {
            request.setEntity(new UrlEncodedFormEntity(parameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unsupported Encoding Exception in post parameter encoding: %s",e.getMessage());
            throw new CMDBException(String.format("Unsupported Encoding Exception in post parameter encoding: %s",e.getMessage()));
        }
    }

    protected String postRequest( String uri, String body ) throws CMDBBadStatusException {
        String json = null;
        HttpPost request = null;
        try {
            request = new HttpPost(uri);
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer: "+ getAccessToken());
            logger.trace("HTTP Method: %s with body: '%s'",request, body);
            HttpClient client = HttpClientFactory.getHttpClient(request.getURI().getHost());
            request.setEntity( new StringEntity(body));
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            json = client.execute( request, this.responseHandler);
            logger.trace("Data Returned: '%s'", json);
        } catch (CMDBBadStatusException badStatusException) {
            badStatusException.setURL(request.getURI().toString());
            throw badStatusException;
        } catch (Exception e) {
            logger.warn("Exception: %s",e.getMessage());
        }
        return json;
    }


    protected String getRequest( String uri, Map<String,String> parameters ) throws CMDBBadStatusException {
        String json = null;
        HttpGet request = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            for( String key : parameters.keySet() )
                uriBuilder.addParameter(key, parameters.get(key));
            request = new HttpGet(uriBuilder.build());
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer: "+ getAccessToken());
            logger.trace("HTTP Method: %s",request);
            HttpClient client = HttpClientFactory.getHttpClient(request.getURI().getHost());
            json = client.execute(request, this.responseHandler);
            logger.trace("Data Returned: '%s'",json);
        } catch (CMDBBadStatusException badStatusException) {
            badStatusException.setURL(request.getURI().toString());
            throw badStatusException;
        } catch (Exception e) {
            logger.warn("Exception: %s",e.getMessage());
        }
        return json;
    }
}
