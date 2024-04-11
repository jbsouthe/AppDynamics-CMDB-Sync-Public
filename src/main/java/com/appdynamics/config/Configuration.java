package com.appdynamics.config;

import com.appdynamics.cmdb.CMDBClient;
import com.appdynamics.cmdb.EntityType;
import com.appdynamics.cmdb.authentication.ApigeeOAuthClient;
import com.appdynamics.cmdb.authentication.IOAuthClient;
import com.appdynamics.cmdb.authentication.SNOWOAuthClient;
import com.appdynamics.config.jaxb.CMDBSync;
import com.appdynamics.config.jaxb.Entry;
import com.appdynamics.config.jaxb.Identity;
import com.appdynamics.config.jaxb.OAuth;
import com.appdynamics.config.jaxb.Table;
import com.appdynamics.controller.Controller;
import com.appdynamics.controller.apidata.model.ITaggable;
import com.appdynamics.cryptography.AES256Cryptography;
import com.appdynamics.cryptography.ICryptography;
import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.exceptions.InvalidConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    private static final Logger logger = LogManager.getFormatterLogger();
    public static final String SYNC_FREQUENCY_MINUTES_PROPERTY = "sync-frequency-minutes";
    public static final String CACHE_TIMEOUT_MINUTES_PROPERTY = "cache-timeout-minutes";
    public static final String NUMBER_EXEC_THREADS_PROPERTY = "number-execute-threads";
    public static final String MAX_BATCH_RETRY_ATTEMPTS_PROPERTY = "max-batch-retries";

    private Properties properties = new Properties();
    private Controller controller;
    private CMDBClient cmdbClient;
    private IOAuthClient oAuthClient;
    private ICryptography cryptography = null;
    private Map<EntityType, ITable> cmdbTableMap = new HashMap<>();
    private boolean running=true;
    private CMDBSync configXML;

    public String getProperty( String key ) {
        return getProperty(key, (String)null);
    }
    public String getProperty( String key , String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }
    public Boolean getProperty( String key, Boolean defaultBoolean) {
        return Boolean.parseBoolean( getProperty(key, defaultBoolean.toString()));
    }
    public Long getProperty( String key, Long defaultLong ) {
        return Long.parseLong( getProperty(key, defaultLong.toString()));
    }
    public Integer getProperty( String key, Integer defaultInteger ) {
        return Integer.parseInt( getProperty(key, defaultInteger.toString()));
    }

    public Controller getController() { return controller; }
    public CMDBClient getCmdbClient() { return cmdbClient; }
    public IOAuthClient getOAuthClient() { return oAuthClient; }

    //Only for JUnit testing
    public Configuration() {}

    public Configuration ( String configFileName ) throws Exception {
        this(configFileName, null, null);
    }

    public Configuration( String configFileName, String section, String keyFileName ) throws Exception {
        logger.info("Processing Config File: %s", configFileName);
        File configFile = new File(configFileName);
        if( !configFile.exists() ) throw new InvalidConfigurationException(String.format("Config file '%s' does not exist", configFileName) );
        if( !configFile.canRead() ) throw new InvalidConfigurationException(String.format("Config file '%s' is not readable", configFileName) );
        if( !configFile.isFile() ) throw new InvalidConfigurationException(String.format("Config file '%s' is not a file", configFileName) );
        if( configFile.length() == 0 ) throw new InvalidConfigurationException(String.format("Config file '%s' is an empty file", configFileName) );

        if( keyFileName != null ) {
            this.cryptography = new AES256Cryptography(keyFileName);
        }

        try {
            // Creating JAXB Context for CMDBSync class
            JAXBContext jaxbContext = JAXBContext.newInstance(CMDBSync.class);

            // Creating Unmarshaller
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Unmarshalling XML file to CMDBSync object
            configXML = (CMDBSync) jaxbUnmarshaller.unmarshal(configFile);
        } catch (JAXBException jaxbException) {
            throw new InvalidConfigurationException(String.format("XML Format error: %s",jaxbException.toString()));
        }

        setSyncFrequencyMinutes(configXML.getSyncFrequencyMinutes());
        setCacheTimeoutMinutes(configXML.getCacheTimeoutMinutes());
        setNumberOfExecuteThreads(configXML.getNumberOfExecuteThreads());
        setMaxBatchRetryAttempts(configXML.getMaxBatchRetryAttempts());

        if( section == null || section.equals("encryption")) {
            setEncryption( configXML.getAes256() );
        }

        if( section == null || section.equals("cmdb") ) {
            setOAuthClient( configXML.getOauth() );

            for(Table tableXML : configXML.getTables())
                addCMDBTable(tableXML.getType(), tableXML.isEnabled(), tableXML.getUrl(), tableXML.getIdentifyingSysParm(),
                        tableXML.getParentIdentifyingSysParm(), tableXML.getSysParms(), tableXML.getCacheTimeoutMinutes());

            if( configXML.getConversionMap() != null ) {
                for (Entry entry : configXML.getConversionMap().getEntries())
                    addMapEntry(entry.getType(), entry.getAppDynamicsName(), entry.getCmdbName(), entry.getParent());

                for (Identity identity : configXML.getConversionMap().getIdentities())
                    addIdentityConversion(identity.getType(), identity.getFrom(), identity.getTo());
            }
        }

        if( section == null || section.equals("controller") ) {
            //Target controller section, this is where we plan to create insertable data for
            addController( configXML.getController().getUrl(), configXML.getController().getClientId(),
                    configXML.getController().getClientSecret().getValue(), configXML.getController().getClientSecret().isEncrypted());
        }

        logger.info("Validating Configured Settings");
    }

    public void setEncryption( String keyFileName) throws InvalidConfigurationException {
        try {
            if( keyFileName != null )
                this.cryptography = new AES256Cryptography(keyFileName);
        } catch (Exception e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    public String encrypt( String plainText ) throws Exception {
        if( this.cryptography == null )
            throw new InvalidConfigurationException("Encryption not configured, can not encrypt anything");
        return this.cryptography.encrypt(plainText);
    }

    private String decrypt( String encryptedString ) throws Exception {
        if( this.cryptography == null )
            throw new InvalidConfigurationException("Encryption not configured, can not decrypt anything");
        return this.cryptography.decrypt(encryptedString);
    }

    private Map<String,Map<String, String>> identityMap = new HashMap<>();
    public void addIdentityConversion( String type, String from, String to) throws InvalidConfigurationException {
        if( type == null ) type = "Global";
        if( from == null ) throw new InvalidConfigurationException("Invalid Identity Conversion \"from\" must be specified");
        if( to == null ) throw new InvalidConfigurationException("Invalid Identity Conversion \"to\" must be specified");
        Map<String,String> map = this.identityMap.get(type);
        if( map == null ) {
            map = new HashMap<>();
            this.identityMap.put(type,map);
        }
        map.put(from,to);
    }
    public String getIdentityConversion( EntityType type, String from ) {
        Map<String,String> map = (type == null ? this.identityMap.get("Global") : this.identityMap.get(type.name()) );
        if( map == null && type == null ) return null;
        String value = map.get(from);
        if( value == null && type != null ) {
            return getIdentityConversion(null, from);
        }
        return value;
    }
    public boolean isIdentityConversionConfigured() { return this.identityMap.size() > 0; }

    private Map<String, EntityConverter> conversionMap = new HashMap<>();
    public void addMapEntry( String type, String appDName, String cmdbName, Entry parentEntry) {
        String parentType = null, parentAppDName = null, parentCMDBName = null;
        if( parentEntry != null ) {
            parentType = parentEntry.getType();
            parentAppDName = parentEntry.getAppDynamicsName();
            parentCMDBName = parentEntry.getCmdbName();
        }
        EntityConverter mapEntry = new EntityConverter(type, appDName, cmdbName, parentType, parentAppDName, parentCMDBName);
        conversionMap.put(mapEntry.getKey(), mapEntry);
    }

    public EntityConverter getConversionMap (ITable table, ITaggable identifier) {
        return conversionMap.get(EntityConverter.makeKey(table,identifier));
    }

    private void setMaxBatchRetryAttempts (int count) {
        this.properties.put(MAX_BATCH_RETRY_ATTEMPTS_PROPERTY, count);
    }

    private void setNumberOfExecuteThreads (int count) {
        this.properties.put(NUMBER_EXEC_THREADS_PROPERTY, count);
    }

    private void setCacheTimeoutMinutes (int minutes) {
        this.properties.put(CACHE_TIMEOUT_MINUTES_PROPERTY, minutes);
    }

    private void setSyncFrequencyMinutes (int minutes) {
        this.properties.put(SYNC_FREQUENCY_MINUTES_PROPERTY, minutes);
    }

    public void setSyncFrequencyMinutes( String minutes ) throws InvalidConfigurationException {
        try {
            Integer.parseInt(minutes);
            this.properties.put(SYNC_FREQUENCY_MINUTES_PROPERTY, minutes);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    public void setCacheTimeoutMinutes( String minutes ) throws InvalidConfigurationException {
        try {
            Integer.parseInt(minutes);
            this.properties.put(CACHE_TIMEOUT_MINUTES_PROPERTY, minutes);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    public void setNumberOfExecuteThreads( String count ) throws InvalidConfigurationException {
        try {
            Integer.parseInt(count);
            this.properties.put(NUMBER_EXEC_THREADS_PROPERTY, count);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    public void setMaxBatchRetryAttempts( String count ) throws InvalidConfigurationException {
        try {
            Integer.parseInt(count);
            this.properties.put(MAX_BATCH_RETRY_ATTEMPTS_PROPERTY, count);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    private void setOAuthClient (OAuth oauth) throws InvalidConfigurationException {
        String clientSecret = null, password = null;
        boolean clientSecretEncrypted = false, passwordEncrypted = false;
        if( oauth.getClientSecret() != null ) {
            clientSecret = oauth.getClientSecret().getValue();
            clientSecretEncrypted = oauth.getClientSecret().isEncrypted();
        }
        if( oauth.getPassword() != null ) {
            password = oauth.getPassword().getValue();
            passwordEncrypted = oauth.getPassword().isEncrypted();
        }
        setOAuthClient( oauth.getType(), oauth.getUrl(), oauth.getClientId(),
                clientSecret, clientSecretEncrypted, oauth.getClientScope(),
                oauth.getUserName(), password, passwordEncrypted);
    }

    public void setOAuthClient( String type, String url, String id, String secret, Boolean secretEncryptedFlag, String scope, String userName, String password, Boolean passwordEncryptedFlag) throws InvalidConfigurationException {
        secret = getEnvironmentVar(secret);
        password = getEnvironmentVar(password);
        try {
            if (secretEncryptedFlag != null && secretEncryptedFlag)
                secret = decrypt(secret);
            if (passwordEncryptedFlag != null && passwordEncryptedFlag)
                password = decrypt(password);
        } catch (Exception e) {
            throw new InvalidConfigurationException("Error decrypting config parameter. Message: "+ e);
        }
        switch(type.toLowerCase()) {
            case "apigee": {
                this.oAuthClient = new ApigeeOAuthClient(url, id, secret, scope, this);
                break;
            }
            case "servicenow": {
                this.oAuthClient = new SNOWOAuthClient(url, id, secret, userName, password, this);
                break;
            }
            default: throw new InvalidConfigurationException("Unknown and unimplemented OAuth Client, use Apigee until we support more");
        }
        try {
            String token = this.oAuthClient.getAccessToken();
            assert token.length() > 0;
        } catch (CMDBException e) {
            throw new InvalidConfigurationException("Oauth client threw an exception when trying to get an Access Token: "+e);
        }
        this.cmdbClient = new CMDBClient(this);
    }

    public void addCMDBTable( String typeString, boolean enabled, String url, String identifyingSysParm, String parentIdentifyingSysParm, String sysParms, int cacheTimeoutMinutesArg ) throws InvalidConfigurationException {
        addCMDBTable(typeString, enabled, url, identifyingSysParm, parentIdentifyingSysParm, sysParms, cacheTimeoutMinutesArg, false);
    }

    public void addCMDBTable( String typeString, boolean enabled, String url, String identifyingSysParm, String parentIdentifyingSysParm, String sysParms, int cacheTimeoutMinutesArg , boolean forceOverWrite) throws InvalidConfigurationException {
        EntityType type = EntityType.valueOfIgnoreCase(typeString);
        if ( !forceOverWrite && this.cmdbTableMap.containsKey(type)) throw new InvalidConfigurationException("CMDB Table Type "+ type +" is defined twice in the configuration file");
        int cacheTimeoutMinutes = getProperty(CACHE_TIMEOUT_MINUTES_PROPERTY, 20);
        if( cacheTimeoutMinutesArg > -1 )
            cacheTimeoutMinutes = cacheTimeoutMinutesArg;
        this.cmdbTableMap.put(type, new TableConfig(type, enabled, url, identifyingSysParm, parentIdentifyingSysParm, sysParms, cacheTimeoutMinutes));
        logger.info("Adding CMDB Table: %s", this.cmdbTableMap.get(type));
    }

        public ITable getTableConfig( EntityType type ) {
        logger.trace("getTableConfig('%s') returns: ", type, this.cmdbTableMap.get(type));
        return this.cmdbTableMap.get(type);
    }

    public void addController( String url, String clientId, String clientSecret, Boolean clientSecretEncryptedFlag ) throws InvalidConfigurationException {
        clientSecret = getEnvironmentVar(clientSecret);
        if (clientSecretEncryptedFlag != null && clientSecretEncryptedFlag) {
            try {
                clientSecret = decrypt(clientSecret);
            } catch (Exception e) {
                throw new InvalidConfigurationException(e.getMessage());
            }
        }
        this.controller = new Controller( url, clientId, clientSecret,this);
    }

    public String getEnvironmentVar (String secret) throws InvalidConfigurationException {
        if( secret == null ) return null;
        if( secret.matches("^\\$[A-Za-z_][A-Za-z0-9_]*$") ) {
            String variable = System.getenv(secret.substring(1));
            if( variable == null || variable.isEmpty() )
                throw new InvalidConfigurationException(String.format("Environment Variable '%s' is not set?", secret));
            return variable;
        }
        return secret;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean b) { this.running=b; }
}
