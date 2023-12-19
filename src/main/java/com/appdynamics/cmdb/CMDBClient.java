package com.appdynamics.cmdb;

import com.appdynamics.cmdb.authentication.IOAuthClient;
import com.appdynamics.config.Configuration;
import com.appdynamics.config.EntityConverter;
import com.appdynamics.config.ITable;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.cmdb.Tag;
import com.appdynamics.controller.apidata.model.ITaggable;
import com.appdynamics.exceptions.CMDBBadStatusException;
import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.util.HttpClientFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CMDBClient {
    private static final Logger logger = LogManager.getFormatterLogger();
    private static final Logger QUERY_LOGGER = LogManager.getFormatterLogger("QUERY_LOGGER");

    private IOAuthClient oAuthClient;
    private ArrayList<NameValuePair> authParameters = new ArrayList<>();
    private Configuration configuration = null;
    protected Gson gson = null;
    private CacheManager<String,IResult> cacheManager = new CacheManager<String, IResult>();
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

    public CMDBClient (Configuration configuration) {
        this.oAuthClient = configuration.getOAuthClient();
        this.configuration = configuration;
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        //builder.registerTypeAdapter(ResultObject.class, new ResultObject.ResultObjectDeserializer());
        //builder.registerTypeAdapter(ResultObject.class, new ResultObject.ResultObjectSerializer());
        this.gson = builder.create();
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
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer: "+ this.oAuthClient.getAccessToken());
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


    public String getRequest (String uri, Map<String, String> parameters) throws CMDBBadStatusException {
        String json = null;
        HttpGet request = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            for (String key : parameters.keySet())
                uriBuilder.addParameter(key, parameters.get(key));
            request = new HttpGet(uriBuilder.build());
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.oAuthClient.getAccessToken());
            request.setHeader("Accept", "application/json");
            logger.trace("HTTP Method: %s", request);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (CMDBException e) {
            logger.warn("Error getting access token for the cmdb: "+ e.getMessage(),e);
            return null;
        }
        HttpClient client = HttpClientFactory.getHttpClient(request.getURI().getHost());
            int tries=0;
            boolean succeeded=false;
            while( !succeeded && tries < 3 ) {
                try {
                    logger.trace("Request: "+ request);
                    for( Header header : request.getAllHeaders()) logger.trace("Header: "+ header.toString());
                    json = client.execute(request, this.responseHandler);
                    succeeded=true;
                } catch (IOException e) {
                    logger.error("Exception in attempting to get cmdb data, Exception: %s", e.getMessage());
                    tries++;
                } catch (IllegalStateException illegalStateException) {
                    tries++;
                    client = HttpClientFactory.getHttpClient(request.getURI().getHost(),true);
                    logger.warn("Caught exception on connection, building a new connection for retry, Exception: %s", illegalStateException.getMessage());
                }
            }
            if( !succeeded ) {
                throw new CMDBBadStatusException("CMDB Requests Failed after many retries", json, uri);
            }

            logger.trace("Data Returned: '%s'",json);

        return json;
    }

    public Entity query( ITaggable component ) throws CMDBException {
        ITable table = configuration.getTableConfig(component.getEntityType());
        if (table == null) throw new CMDBException("No known CMDB Table Configuration for type: "+ component.getEntityType());
        if( ! table.isEnabled() ) {
            logger.debug("Table type is disabled in configuration: %s",table.getType());
            return null;
        }
        IResult resultObject = getResultCache(table, component);
        if( resultObject == null || resultObject.isEmpty() ) {
            return null;
        }
        Map<String,String> map = resultObject.getResult();
        Entity entity = new Entity(component.getName(), component.getId() );
        for( String key : map.keySet()) {
            String value = map.get(key);
            if( value != null )
                entity.tags.add( new Tag(key,map.get(key) ) );
        }
        return entity;
   }

   private IResult getResultCache(ITable table, ITaggable identifier) throws CMDBException {
       IResult resultObject = cacheManager.get(identifier.toKey(), table.getCacheTimeoutMinutes());
       if( resultObject == null ) {
           //either we don't have a cache or it is timed out for this table/composite
           resultObject = cacheManager.put(identifier.toKey(), getCMDBQuery(table, identifier));
       }
       return resultObject;
   }



    private ResultObject getCMDBQuery (ITable table, ITaggable identifier) {
        Map<String,String> parameters = new HashMap<>();
        StringBuilder query = new StringBuilder();
        //Apply Conversion Map, if it exists for this entity
        EntityConverter mapEntry = configuration.getConversionMap( table, identifier);
        if( mapEntry != null ) {
            query.append( mapEntry.getQuery(table, identifier) );
        } else {
            query.append(table.getIdentifyingSysParm() + "=" + identifier.getName());
            if (table.getParentIdentifyingSysParm() != null)
                query.append("^" + table.getParentIdentifyingSysParm() + "=" + identifier.getParentName());
        }
        logger.debug("CMDB Query String: '%s'",query.toString());
        parameters.put("sysparm_query", query.toString() );
        if( table.getSysParamsToPopulate() != null )
            parameters.put("sysparm_fields", table.getSysParamsToPopulate());
        parameters.put("sysparm_display_value","true");
        parameters.put("sysparm_exclude_reference_link","true");
        String json = null;
        try {
            json = getRequest(table.getTableURL(), parameters);
        } catch (CMDBBadStatusException e) {
            logger.warn("Error attempting to get CMDB data: "+ e.getMessage(), e);
        }
        ResultObject resultObject = gson.fromJson(json, ResultObject.class);
        if( resultObject == null || resultObject.isEmpty() ) {
            QUERY_LOGGER.warn("Missing CMDB Object: " + identifier);
        } else {
            resultObject.setKeyField(table.getIdentifyingSysParm());
        }
        return resultObject;
    }
}
