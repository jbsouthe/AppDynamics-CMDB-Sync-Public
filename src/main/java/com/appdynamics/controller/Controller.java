package com.appdynamics.controller;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.account.MyAccount;
import com.appdynamics.controller.apidata.cmdb.BatchDeleteRequest;
import com.appdynamics.controller.apidata.cmdb.BatchResponse;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.Model;
import com.appdynamics.controller.apidata.model.Node;
import com.appdynamics.controller.apidata.model.ServiceEndpoint;
import com.appdynamics.controller.apidata.model.Tier;
import com.appdynamics.controller.apidata.model.TreeNode;
import com.appdynamics.controller.apidata.synthetic.SyntheticList;
import com.appdynamics.controller.apidata.synthetic.page.SyntheticPage;
import com.appdynamics.controller.apidata.synthetic.page.SyntheticPageListRequest;
import com.appdynamics.controller.apidata.synthetic.page.SyntheticPageListResponse;
import com.appdynamics.exceptions.ControllerBadStatusException;
import com.appdynamics.exceptions.InvalidConfigurationException;
import com.appdynamics.util.HttpClientFactory;
import com.appdynamics.util.Parser;
import com.appdynamics.util.TimeUtil;
import com.appdynamics.controller.apidata.auth.AccessToken;
import com.appdynamics.controller.apidata.model.ApplicationListing;
import com.appdynamics.controller.apidata.model.BusinessTransaction;
import com.appdynamics.controller.apidata.server.Server;
import com.appdynamics.controller.apidata.server.ServerListRequest;
import com.appdynamics.controller.apidata.server.ServerListResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    protected static final Logger logger = LogManager.getFormatterLogger();

    public String hostname;
    public URL url;
    private String clientId, clientSecret;
    private long accountId;
    private AccessToken accessToken = null;
    public Application[] applications = null;
    public Model controllerModel = null;
    protected Gson gson = null;
    private HttpClient client = null;
    protected Configuration configuration;
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
                throw new ControllerBadStatusException(response.getStatusLine().toString(), EntityUtils.toString(response.getEntity()), uri);
            }
        }

    };

    public Controller( String urlString, String clientId, String clientSecret, Configuration configuration ) throws InvalidConfigurationException {
        if( !urlString.endsWith("/") ) urlString+="/"; //this simplifies some stuff downstream
        try {
            this.url = new URL(urlString);
        } catch (Exception exception) {
            throw new InvalidConfigurationException(String.format("Bad url in configuration for a controller: %s exception: %s", urlString, exception.toString()));
        }
        this.hostname = this.url.getHost();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.applications = applications;
        this.client = HttpClientFactory.getHttpClient(this.hostname);
        this.configuration = configuration;
        this.accountId = getAccountId();
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        //builder.registerTypeAdapter(Tag.class, new Tag.TagDeserializer());
        //builder.registerTypeAdapter(Tag.class, new Tag.TagSerializer());
        this.gson = builder.create();
        getModel(); //initialize model
    }

    public Configuration getConfiguration() { return configuration; }

    public Application getApplication( long id ) {
        for( Application application : getModel().getApplications() )
            if( application.id == id ) return application;
        return null;
    }

    public String getBearerToken() {
        if( isAccessTokenExpired() && !refreshAccessToken()) return null;
        return "Bearer "+ accessToken.access_token;
    }

    private boolean isAccessTokenExpired() {
        long now = new Date().getTime();
        if( accessToken == null || accessToken.expires_at < now ) return true;
        return false;
    }

    private boolean refreshAccessToken() { //returns true on successful refresh, false if an error occurs
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(clientId, clientSecret);
        logger.trace("credentials configured: %s",credentials.toString());
        provider.setCredentials(AuthScope.ANY, credentials);
        logger.trace("provider configured: %s",provider.toString());
        HttpPost request = new HttpPost(url.toString()+"/controller/api/oauth/access_token");
        //request.addHeader(HttpHeaders.CONTENT_TYPE,"application/vnd.appd.cntrl+protobuf;v=1");
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add( new BasicNameValuePair("grant_type","client_credentials"));
        postParameters.add( new BasicNameValuePair("client_id",clientId));
        postParameters.add( new BasicNameValuePair("client_secret",clientSecret));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unsupported Encoding Exception in post parameter encoding: %s",e.getMessage());
        }

        if( logger.isTraceEnabled()){
            logger.trace("Request to run: %s",request.toString());
            for( Header header : request.getAllHeaders())
                logger.trace("with header: %s",header.toString());
        }

        HttpResponse response = null;
        int tries=0;
        boolean succeeded=false;
        while( !succeeded && tries < 3 ) {
            try {
                response = client.execute(request);
                succeeded=true;
                logger.trace("Response Status Line: %s", response.getStatusLine());
            } catch (IOException e) {
                logger.error("Exception in attempting to get access token, Exception: %s", e.getMessage());
                tries++;
            } catch (IllegalStateException illegalStateException) {
                tries++;
                this.client = HttpClientFactory.getHttpClient(this.hostname,true);
                logger.warn("Caught exception on connection, building a new connection for retry, Exception: %s", illegalStateException.getMessage());
            }
        }
        if( !succeeded ) return false;
        HttpEntity entity = response.getEntity();
        Header encodingHeader = entity.getContentEncoding();
        Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 : Charsets.toCharset(encodingHeader.getValue());
        String json = null;
        try {
            json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            logger.trace("JSON returned: %s",json);
        } catch (IOException e) {
            logger.warn("IOException parsing returned encoded string to json text: "+ e.getMessage());
            return false;
        }
        if( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logger.warn("Access Key retreival returned bad status: %s message: %s", response.getStatusLine(), json);
            return false;
        }
        this.accessToken = gson.fromJson(json, AccessToken.class); //if this doesn't work consider creating a custom instance creator
        this.accessToken.expires_at = new Date().getTime() + (accessToken.expires_in*1000); //hoping this is enough, worry is the time difference
        return true;
    }


    public TreeNode[] getApplicationMetricFolders(Application application, String path) {
        String json = null;

        int tries=0;
        boolean succeeded=false;
        while( !succeeded && tries < 3 ) {
            try {
                if ("".equals(path)) {
                    json = getRequest(String.format("controller/rest/applications/%s/metrics?output=JSON", Parser.encode(application.name)));
                } else {
                    json = getRequest(String.format("controller/rest/applications/%s/metrics?metric-path=%s&output=JSON", Parser.encode(application.name), Parser.encode(path)));
                }
                succeeded=true;
            } catch (ControllerBadStatusException controllerBadStatusException) {
                tries++;
                logger.warn("Try %d failed for request to get app application metric folders for %s with error: %s",tries,application.name,controllerBadStatusException.getMessage());
            }
        }
        if(!succeeded) logger.warn("Failing on get of application metric folder, controller may be down");

        TreeNode[] treeNodes = null;
        try {
            treeNodes = gson.fromJson(json, TreeNode[].class);
        } catch (JsonSyntaxException jsonSyntaxException) {
            logger.warn("Error in parsing returned text, this may be a bug JSON '%s' Exception: %s",json, jsonSyntaxException.getMessage());
        }
        return treeNodes;
    }


    protected String postRequest( String requestUri, String body ) throws ControllerBadStatusException {
        HttpPost request = new HttpPost(String.format("%s%s", this.url.toString(), requestUri));
        request.addHeader(HttpHeaders.AUTHORIZATION, getBearerToken());
        logger.trace("HTTP Method: %s with body: '%s'",request, body);
        String json = null;
        try {
            request.setEntity( new StringEntity(body));
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            json = client.execute( request, this.responseHandler);
            logger.trace("Data Returned: '%s'", json);
        } catch (ControllerBadStatusException controllerBadStatusException) {
            logger.warn("Failed Request: "+ requestUri +" with body: "+ body);
            controllerBadStatusException.setURL(request.getURI().toString());
            throw controllerBadStatusException;
        } catch (IOException e) {
            logger.warn("Exception: %s",e.getMessage());
        }
        return json;
    }

    public String getRequest( String formatOrURI, Object... args ) throws ControllerBadStatusException {
        if( args == null || args.length == 0 ) return getRequest(formatOrURI);
        return getRequest( String.format(formatOrURI,args));
    }

    protected String getRequest( String uri ) throws ControllerBadStatusException {
        HttpGet request = new HttpGet(String.format("%s%s", this.url.toString(), uri));
        request.addHeader(HttpHeaders.AUTHORIZATION, getBearerToken());
        logger.trace("HTTP Method: %s",request);
        String json = null;
        try {
            json = client.execute(request, this.responseHandler);
            logger.trace("Data Returned: '%s'",json);
        } catch (ControllerBadStatusException controllerBadStatusException) {
            controllerBadStatusException.setURL(request.getURI().toString());
            throw controllerBadStatusException;
        } catch (IOException e) {
            logger.warn("Exception: %s",e.getMessage());
        }
        return json;
    }

    Map<String,Long> _applicationIdMap = null;
    public long getApplicationId( String name ) {
        logger.trace("Get Application id for %s",name);
        if( _applicationIdMap == null ) { //go get em
            try {
                String json = getRequest("controller/restui/applicationManagerUiBean/getApplicationsAllTypes");
                ApplicationListing applicationListing = gson.fromJson(json, ApplicationListing.class);
                _applicationIdMap = new HashMap<>();
                for (Application app : applicationListing.getApplications() )
                    if( app.active ) _applicationIdMap.put(app.name, app.id);
            } catch (ControllerBadStatusException controllerBadStatusException) {
                logger.warn("Giving up on getting application id, not even going to retry");
            }
        }
        if( !_applicationIdMap.containsKey(name) ) return -1;
        return _applicationIdMap.get(name);
    }

    public List<ServiceEndpoint> getServiceEndpoints(long applicationId ) throws ControllerBadStatusException {
        long timestamp = TimeUtil.now();
        return getServiceEndpoints(applicationId, timestamp-60000, timestamp);
    }

    public List<ServiceEndpoint> getServiceEndpoints( long applicationId, long startTimestamp, long endTimestamp ) throws ControllerBadStatusException {
        String postBody = String.format("{\"requestFilter\":{\"queryParams\":{\"applicationId\":%d,\"mode\":\"FILTER_EXCLUDED\"},\n" +
                "    \"searchText\":\"\",\n" +
                "    \"filters\":{\"sepPerfData\":{\"responseTime\":0,\"callsPerMinute\":0},\"type\":[],\"sepName\":[]}},\n" +
                "    \"columnSorts\":[{\"column\":\"NAME\",\"direction\":\"ASC\"}],\n" +
                "    \"timeRangeStart\":%d,\n" +
                "    \"timeRangeEnd\":%d}", applicationId, startTimestamp, endTimestamp);
        String json = postRequest("controller/restui/serviceEndpoint/list", postBody);
        ServiceEndpointResponse serviceEndpointResponse = gson.fromJson(json, ServiceEndpointResponse.class);
        if( serviceEndpointResponse != null ) return serviceEndpointResponse.data;
        return null;
    }

    public Model getModel() {
        return getModel(false);
    }

    public Model getModel( boolean forceRebuildOfCache ) {
        if( this.controllerModel == null || forceRebuildOfCache ) {
            logger.info("Initializing Model for Controller: %s",url);
            try {
                String json = getRequest("controller/restui/applicationManagerUiBean/getApplicationsAllTypes");
                logger.trace("getApplicationsAllTypes returned: %s",json);
                this.controllerModel = new Model(gson.fromJson(json, ApplicationListing.class));
                for (Application application : this.controllerModel.getAPMApplications()) {
                    logger.info("Creating Model for Application: %s",application.name);
                    application.setController(this);
                }
            } catch (ControllerBadStatusException controllerBadStatusException) {
                logger.warn("Giving up on getting controller model, not even going to retry");
            }
        }
        return this.controllerModel;
    }

    public long getAccountId() {
        if( accountId == -1 ) {
            try {
                String json = getRequest("controller/api/accounts/myaccount?output=json");
                MyAccount account = gson.fromJson(json, MyAccount.class);
                this.accountId = Long.parseLong(account.id);
                logger.debug("Fetched the account id for '%s' controller in the SaaS: %d", account.name, accountId);
            } catch (ControllerBadStatusException e) {
                logger.warn("Could not get accountId, this means something is wrong with our controller communication, most likley the entire run will fail, returning -1");
            }
        }
        return accountId;
    }

    public void discardToken() {
        this.accessToken=null;
    }

    public List<Server> getServerList () throws ControllerBadStatusException {
        String json = postRequest("controller/sim/v2/user/machines/keys", gson.toJson( new ServerListRequest()));
        ServerListResponse serverListResponse = gson.fromJson(json, ServerListResponse.class);
        return serverListResponse.machineKeys;
    }

    public List<SyntheticPage> getSyntheticPageList (Application eumApplication) throws ControllerBadStatusException {
        String json = postRequest("controller/restui/web/pagelist", gson.toJson(new SyntheticPageListRequest(eumApplication.id)));
        SyntheticPageListResponse response = gson.fromJson(json, SyntheticPageListResponse.class);
        if( response != null )
            return response.data;
        return new ArrayList<>();
    }

    public String getTags (String typeString, String possibleApp, String[] names) {
        EntityType type = EntityType.valueOfIgnoreCase(typeString);
        List<Long> idList = null;
        switch(type) {
            case Server -> idList = getServerIds(possibleApp, names);
            case Application -> idList = getAppIds(possibleApp, names);
            case Tier -> idList = getTierIds(possibleApp, names);
            case Node -> idList = getNodeIds(possibleApp, names);
            case BusinessTransaction -> idList = getBTIds(possibleApp, names);
            case SyntheticPage -> idList = getSyntheticPageIds(possibleApp, names);
            default -> throw new IllegalArgumentException("EntityType has new entities but the BatchTaggingRequest Constructor was not updated");
        }
        if( idList == null || idList.isEmpty() ) {
            return "No Items Found To Delete Tags For";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for( Long id : idList ) {
            try {
                stringBuilder.append( String.format("Tags for id: %d type: %s response: ",id, type));
                stringBuilder.append( getRequest("controller/restui/tags?entityId=%d&entityType=%s", id, type.convertToAPIEntityType()) );
                stringBuilder.append("\n");
            } catch (ControllerBadStatusException e) {
                stringBuilder.append(String.format("Error retrieving tags for id: %d Exception: %s", id, e));
            }
        }
        return stringBuilder.toString();
    }

    public Long getServerId (String name) throws ControllerBadStatusException {
        for( Server server : getServerList() ) {
            if( server.serverName.equalsIgnoreCase(name)) return server.machineId;
        }
        return null;
    }

    public class ServiceEndpointResponse{
        public List<ServiceEndpoint> data;
    }

    public BatchResponse updateTags(BatchTaggingRequest batchTaggingRequest) throws ControllerBadStatusException {
        String json = postRequest("controller/restui/tags/tagEntitiesInBatch", gson.toJson(batchTaggingRequest));
        return gson.fromJson(json, BatchResponse.class);
    }

    public SyntheticList getSyntheticScriptList(Application syntheticApp) throws ControllerBadStatusException {
        String json = postRequest("controller/restui/synthetic/schedule/getJobList/" + syntheticApp.id, "");
        return gson.fromJson(json, SyntheticList.class);
    }

    public String deleteTags( String typeString, String possibleApp, String...names ) {
        EntityType type = EntityType.valueOfIgnoreCase(typeString);
        List<Long> idList = null;
        switch(type) {
            case Server -> idList = getServerIds(possibleApp, names);
            case Application -> idList = getAppIds(possibleApp, names);
            case Tier -> idList = getTierIds(possibleApp, names);
            case Node -> idList = getNodeIds(possibleApp, names);
            case BusinessTransaction -> idList = getBTIds(possibleApp, names);
            case SyntheticPage -> idList = getSyntheticPageIds(possibleApp, names);
            default -> throw new IllegalArgumentException("EntityType has new entities but the BatchTaggingRequest Constructor was not updated");
        }
        if( idList == null || idList.isEmpty() ) {
            return "No Items Found To Delete Tags For";
        }
        BatchDeleteRequest batchDeleteRequest = new BatchDeleteRequest(type, idList);
        String body = gson.toJson(batchDeleteRequest);
        System.out.println("Delete Request: "+ body);
        try {
            return postRequest("controller/restui/tags/removeAllTagsOnEntitiesInBatch", body);
        } catch (ControllerBadStatusException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Long> getSyntheticPageIds (String appName, String[] names) {
        List<Long> list = new ArrayList<>();
        Application application = getModel().getSyntheticApplication(appName);
        if( application == null ) throw new RuntimeException("Application not found");
        Map<String,Long> pages = new HashMap<>();
        try {
            for ( SyntheticPage syntheticPage : getSyntheticPageList(application) )
                pages.put(syntheticPage.getName(), syntheticPage.getId());
        } catch (ControllerBadStatusException e) {
            throw new RuntimeException(e);
        }
        for(String name : names)
            list.add(getIds(pages, name));
        return list;
    }

    private List<Long> getBTIds (String appName, String[] names) {
        List<Long> list = new ArrayList<>();
        Application application = getModel().getApplication(appName);
        if( application == null ) throw new RuntimeException("Application not found");
        Map<String,Long> bts = new HashMap<>();
        for( BusinessTransaction businessTransaction : application.businessTransactions)
            bts.put(businessTransaction.name,businessTransaction.id);
        for(String name : names)
            list.add(getIds(bts, name));
        return list;
    }

    private List<Long> getNodeIds (String appName, String[] names) {
        List<Long> list = new ArrayList<>();
        Application application = getModel().getApplication(appName);
        if( application == null ) throw new RuntimeException("Application not found");
        Map<String,Long> nodes = new HashMap<>();
        for( Node node : application.nodes)
            nodes.put(node.name,node.id);
        for(String name : names)
            list.add(getIds(nodes, name));
        return list;
    }

    private List<Long> getTierIds (String appName, String[] names) {
        List<Long> list = new ArrayList<>();
        Application application = getModel().getApplication(appName);
        if( application == null ) throw new RuntimeException("Application not found");
        Map<String,Long> tiers = new HashMap<>();
        for( Tier tier : application.tiers)
            tiers.put(tier.name,tier.id);
        for(String name : names)
            list.add(getIds(tiers, name));
        return list;
    }

    private Long getIds( Map<String,Long> map, String name ) {
        Long id = Parser.parseLong(name);
        if( id != null ) {
            return id;
        } else {
            return map.get(name);
        }
    }

    private List<Long> getServerIds (String firstName, String...names) {
        Map<String,Long> servers = new HashMap<>();
        List<Long> list = new ArrayList<>();
        try {
            for( Server server : getServerList() ) {
                servers.put(server.getName(), server.getId());
            }
            if( firstName.equals("*") ) {
                list.addAll(servers.values());
                return list;
            }
            list.add( getIds(servers, firstName));
            for( String name : names)
                list.add(getIds(servers, name));
        } catch (ControllerBadStatusException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private List<Long> getAppIds (String firstName, String...names) {
        List<Long> list = new ArrayList<>();
        list.add(getApplicationId(firstName));
        for( String name : names)
            list.add(getApplicationId(name));
        return list;
    }
}
