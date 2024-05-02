# AppDynamics CMDB Sync Utility

[![published](https://static.production.devnetcloud.com/codeexchange/assets/images/devnet-published.svg)](https://developer.cisco.com/codeexchange/github/repo/jbsouthe/AppDynamics-CMDB-Sync-Public)

This utility program is meant to sync CMDB Tags from sources, initially ServiceNow CMDB into AppDynamics.
The AppDynamics Controller version 23.12 will be the first to support tags for Applications, Tiers, Nodes, BTs, and Servers.
This utility is one way to get tags into the controller, let us know in an Issue what other systems to support and we will work with you to add that.
Thanks, John

# How to run this

Please run with Java 16+ JVM, I got a little carried away with some stuff

    java -jar CMDBSyncTool.jar -h

    usage: CMDBSync [-h] [-v] [-c ./config-file.xml] [-k ./keyfile.key] [-l INFO] [command [command ...]]

    Manage ServiceNow CMDB Sync Data into an AppDynamics Controller
    
    positional arguments:
    command                Commands are probably too flexible, some examples include:  {
                            "genkey [key file name]", 
                            "encrypt [string]", 
                            "query [table] [string]",
                            "<get | delete> <EntityType> [Application] [Name Name ...]",
                            "insert <csv file>",
                            "executeScheduler" 
                           } (default: executeScheduler)
    
    named arguments:
    -h, --help                      show this help message and exit
    -v, --version
    -c, --config ./config-file.xml  Use this specific XML config file. (default: default-config.xml)
    -k, --keyfile ./keyfile.key     Use this key for encryption
    -l, --level INFO       Set logging level {TRACE|DEBUG|INFO|WARN|ERROR} (default: INFO)


this assumes the ./lib directory contains the jar files found in the package

## Command Reference

The utility supports some commands to assist with managing tags and the process of configuring automated tag import.

### executeScheduler

This is the default command, if none is specified it runs the main scheduler task to fetch tags for components in the controller from the CMDB

### genkey [key file name]

This command generates a new encrpytion key using AES256, the key and initial vector are written to the file specified

### encrypt [string]

This command requires the use of the -k keyfile.key argument, and it then encripts the given string using that key

### query [table] [string]

Query the cmdb data store specified in the configuration file against the [table] url given and using the [string] given on the command line

### <get|delete> <EntityType> [Application] [Name Name ...]

get or delete the keys from the controller, specified in the configuration file, matching the EntityType, and optionally Application, as well as names listed
This command only requires an Application name for Tiers, Nodes, Business Transactions, and Synthetic Pages. For Applications and Servers it is not required.

EntityType can be one of the following: 

    Server
    Application
    Tier
    Node
    BusinessTransaction
    SyntheticPage

example output:

    java -jar CMDBSyncTool.jar get Application mulesoft -c my-config.xml

    Response: Tags for id: 45 type: Application response: [ {
        "id" : 82,
        "key" : "i_tier",
        "value" : "B"
    }, {
        "id" : 86,
        "key" : "i_cmdb_status",
        "value" : "Deployed"
    }, {
        "id" : 87,
        "key" : "i_app_service_offering",
        "value" : "Integration Support Prod"
    }, {
        "id" : 88,
        "key" : "i_app_support_org",
        "value" : "Middleware Platforms"
    }, {
        "id" : 89,
        "key" : "i_app_service",
        "value" : "iPaaS"
    }, {
        "id" : 90,
        "key" : "i_application_name",
        "value" : "MuleSoft Anypoint Platform Internal"
    }, {
        "id" : 91,
        "key" : "i_app_id",
        "value" : "12258"
    } ]

### insert <csv file>

This command will insert the comma separated values into the controller, no CMDB needed for this, but the CSV file has some requirements.
It should have the following format: 

    EntityType,Name,Application,Key1,Key2,...
    Application,mulesoft,,Value1,"Value2, with comma"
    Tier,mulesoft,mulesoft,AnotherValue1,AnotherValue2

The first three columns must be in this order, but the remaining columns can be named anything, these will be the keys. Each row will be mapped by name to the controller  

## Configuration

    <CMDBSync>
    <SyncFrequencyMinutes>20</SyncFrequencyMinutes>
    <CacheTimeoutMinutes>20</CacheTimeoutMinutes>
    <NumberOfExecuteThreads>15</NumberOfExecuteThreads>
    <AES256>keyfile.key</AES256>
    <OAuth type="Apigee">
        <URL>https://server/v1/auth/token</URL>
        <ClientID>xxx</ClientID>
        <ClientSecret encrypted="true">yyy</ClientSecret>
        <ClientScope>zzz</ClientScope>
    </OAuth>
    <Tables>
        <Table type="Server" enabled="true">
            <URL>https://server/itsm/api/now/table/cmdb_ci_server</URL>
            <IdentifyingSysParm>name</IdentifyingSysParm>
            <SysParms>*</SysParms>
        </Table>
        <Table type="Application" enabled="true">
            <URL>https://server/itsm/api/now/table/cmdb_ci_service_discovered</URL>
            <IdentifyingSysParm>business_unit</IdentifyingSysParm>
            <SysParms>u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification</SysParms>
        </Table>
        <Table type="Tier" enabled="true">
            <URL>https://server/itsm/api/now/table/cmdb_ci_service_discovered</URL>
            <IdentifyingSysParm>name</IdentifyingSysParm>
            <ParentIdentifyingSysParm>parent</ParentIdentifyingSysParm>
            <SysParms>parent,u_application_id,name,u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification</SysParms>
        </Table>
        <Table type="Node" enabled="true">
            <URL>https://server/itsm/api/now/table/cmdb_ci_service_discovered</URL>
            <IdentifyingSysParm>name</IdentifyingSysParm>
            <ParentIdentifyingSysParm>parent</ParentIdentifyingSysParm>
            <SysParms>parent,u_application_id,name,u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification</SysParms>
        </Table>
        <Table type="BusinessTransaction" enabled="false">
            <URL>https://server/itsm/api/now/table/cmdb_ci_server</URL>
            <IdentifyingSysParm>business_unit</IdentifyingSysParm>
            <SysParms>parent,u_application_id,name,u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification</SysParms>
        </Table>
    </Tables>
    <Controller>
        <URL>https://controller.saas.appdynamics.com/</URL>
        <ClientID>apiClient@controller</ClientID> <!-- give this account owner and administrator roles -->
        <ClientSecret encrypted="false">xxx</ClientSecret>
    </Controller>
    <ConversionMap>
        <Identity type="Global" from="u_name" to="name" />
        <Entry>
            <Type>Application</Type>
            <AppDynamicsName>mulesoft</AppDynamicsName>
            <CMDBName>12345 - Mulesoft API Gateway</CMDBName>
        </Entry>
    </ConversionMap>
    </CMDBSync>


### Configuration Sections
    
    <CMDBSync>
        <SyncFrequencyMinutes/>
        <CacheTimeoutMinutes/>
        <NumberOfExecuteThreads/>
        <MaxBatchRetryAttempts/>
        <AES256>
        <OAuth/>
        <Tables>
            <Table/>
        </Tables>
        <Controller/>
        <ConversionMap>
            <Identity />
            <Entry/>
        </ConversionMap>
    </CMDBSync>

* SyncFrequencyMinutes - This determines how often to schedule the process of updating the model of the controller, or refreshing the applications, tiers, nodes, ...
* CacheTimeoutMinutes - This is the cache time to keep a CMDB looking without checking again for an entity, it protects the CMDB system from too many queries
* NumberOfExecuteThreads - The number of threads to run collecting CMDB data, only one of these is used to send data to the Controller because of batch operations
* MaxBatchRetryAttempts - The number of times to requeue a batch request if it has an error, default = 3. This is meant to try and catch random connection errors
* AES256 - If encryption is used for the secrets on the tokens, then this must be configured to point to the key file those secrets were encrypted with
* OAuth - This section is the configuration for the CMDB access token, it could be servicenow or if the CMDB is fronted by Apigee, this may be different
* Tables - This section is how an entity in the Controller maps to tables and entities in the CMDB. It is meant to be configured for each customer to allow flexibility
* Controller - This section is for configuring an API Client that has access to the controller, it needs account owner, because we don't yet have refined RBAC for this.
* ConversionMap - Any custom conversions needing to be done in order to map an entity to the CMDB manually

#### AES256 Encryption Configuration

    <AES256>keyfile.key</AES256>

This key can be configured here, or specified on the command line via the -k argument

#### OAuth Configuration

    <OAuth type="Apigee">
        <URL>https://server/v1/auth/token</URL>
        <ClientID>zzz</ClientID>
        <ClientSecret encrypted="false">yyy</ClientSecret> <!-- can be an env variable "$ENV_VARIABLE_NAME" -->
        <ClientScope>xxx</ClientScope>
        <UserName>www</UserName>
        <Password encrypted="false">vvv</Password> <!-- can be an env variable "$ENV_VARIABLE_NAME" -->
    </OAuth>

Depending on the OAuth token provider, some of these fields may not be needed, most important is the list of providers. Supported types:

- Apigee - requires URL, ClientID, ClientSecret (either encrypted or not), and ClientScope
- ServiceNow - requires URL, ClientID, ClientSecret (either encrypted or not), UserName, and Password (either encrypted or not)

#### CMDB Table Configuration

    <Tables>
        <Table type="Node" enabled="true">
            <URL>https://server/itsm/api/now/table/cmdb_ci_service_discovered</URL>
            <IdentifyingSysParm>name</IdentifyingSysParm>
            <ParentIdentifyingSysParm>parent</ParentIdentifyingSysParm>
            <SysParms>parent,u_application_id,name,u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification</SysParms>
            <CacheTimeoutMinutes>###</CacheTimeoutMinutes>
        </Table>
    </Tables>

This section is the most dynamic, and many tables can be defined, but only one table per entity type.

Today, these entity types are supported, and they are based on Controller components:
* Server - Machine agents and Server Viz agents
* Application - APM Applications
* Tier - APM Application Tier
* Node - APM Application Tier Node
* BusinessTransaction - APM Application Tier Business Transaction

These entity types are mapped to the CMDB CIs based on the logic presented in the Table configuration parameters
* the type attribute must be one of the listed types above, and this is required
* the enabled attribute must be either true or false, this allows disabling explicitly a certain Entity Type without deleting the configuration
* URL - each table is defined with a URL to access it
* IdentifyingSysParm - This needs to map directly to a key which will contain a unique identifier value for the Entity being searched
* ParentIdentifyingSysParm - Optionally, a parent can be specified to match to a given Entity parent, for Tiers there is an Application and for Nodes, and BTs, there is a Tier. This is only needed if ambiguity is found in the CMDB data
* SysParams - This is the list of parameters to publish in the Controller for a given match, we have set a limit of 50 per Entity, arbitrarily so we can calculate capacity requirements.
* CacheTimeoutMinutes - This optional attribute allows for a custom cache timeout, maybe Business Transaction CMDB data doesn't change very often and so once a week is enough ~ 10080, this is an integer so don't get crazy

Please let us know if any logical fallacies arise in these options, we will work to improve upon them.

#### Controller Configuration

    <Controller>
        <URL>https://controller.saas.appdynamics.com/</URL>
        <ClientID>apiClient@controller</ClientID> <!-- give this account owner and administrator roles -->
        <ClientSecret encrypted="false">xxx</ClientSecret> <!-- can be an env variable "$ENV_VARIABLE_NAME" -->
        <ModelCacheAge>86400000</ModelCacheAge>
    </Controller>

This utility only runs against one controller, it will need to be run separately for each with a unique configuration file to update another
* URL - the controller access url
* ClientID - the api key name with the account name appended after an '@'
* ClientSecret - the api key secret used to generate a token (either encrypted or not, default not). This can be an environment variable, if it starts with a $ and has no special characters but _, it will be resolved
* ModelCacheAge - the length of time to allow the controller model to be cached for subsequent executions of this utility, default is 1 day, in milliseconds = 86400000

#### Conversion Mapping AppDynamics to CMDB Entities

    <ConversionMap>
        <Identity from="u_application_name" to="Application Name" />
        <Entry>
            <Type>Application</Type>
            <AppDynamicsName>mulesoft</AppDynamicsName>
            <CMDBName>12345 - Mulesoft API Gateway</CMDBName>
        </Entry>
        <Entry>
            <Type>Tier</Type>
            <AppDynamicsName>mulesoft</AppDynamicsName>
            <CMDBName>Mulesoft API Gateway Instance</CMDBName>
            <Parent>
                <Type>Application</Type>
                <AppDynamicsName>mulesoft</AppDynamicsName>
                <CMDBName>12345 - Mulesoft API Gateway</CMDBName>
            </Parent>
        </Entry>
    </ConversionMap>

This example converts the key "u_application_name" to "Application Name" when importing tags to the Controller. In addition, it also converts the application "mulesoft" to "12345 - Mulesoft API Gateway" when pulling tags from the CMDB, and does a similar thing for the mulesoft tier in the mulesoft application
It is only recommended as a temporary technique to map data when names do not match.

## Proxy Configuration

If proxy support is required, set the following arguments before the -jar arguement:

     -Djava.net.useSystemProxies=true

or, to manually specify the host and port:

     -Dhttp.proxyHost=PROXY_HOST
     -Dhttp.proxyPort=PROXY_PORT

or, to manually specify the host, port, and basic user and password:

     -Dhttp.proxyHost=PROXY_HOST
     -Dhttp.proxyPort=PROXY_PORT
     -Dhttp.proxyUser=USERNAME
     -Dhttp.proxyPassword=PASSWORD

or, to manually specify the host, port, and NTLM authentication:

     -Dhttp.proxyHost=PROXY_HOST
     -Dhttp.proxyPort=PROXY_PORT
     -Dhttp.proxyUser=USERNAME
     -Dhttp.proxyPassword=PASSWORD
     -Dhttp.proxyWorkstation=HOSTNAME
     -Dhttp.proxyDomain=NT_DOMAIN
