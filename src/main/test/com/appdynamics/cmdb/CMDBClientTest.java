package com.appdynamics.cmdb;

import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.BusinessTransaction;
import com.appdynamics.controller.apidata.model.Tier;
import com.appdynamics.controller.apidata.server.Server;
import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.exceptions.InvalidConfigurationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CMDBClientTest {
    Configuration configuration = null;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CMDBClientTest () throws Exception {
        Configurator.setAllLevels("", Level.DEBUG);
        try {
            configuration = new Configuration("example-config.xml");
        } catch (Exception e) {
            configuration = null;
        }
    }

    @Test
    void queryServer () throws CMDBException {
        if( configuration == null ) return;
        Server server = new Server();
        server.serverName = "SMEUTILUBU2";
        server.machineId = 1;
        Entity entity = configuration.getCmdbClient().query(server);
        assertTrue(entity != null);
        System.out.println(gson.toJson(entity));
    }

    @Test
    void queryApplication () throws CMDBException {
        if( configuration == null ) return;
        Application application = new Application();
        application.name = "IT Jira";
        application.id = 1;
        Entity entity = configuration.getCmdbClient().query(application);
        assertTrue(entity != null);
        System.out.println(gson.toJson(entity));
    }

    @Test
    void queryTier () throws CMDBException, InvalidConfigurationException {
        if( configuration == null ) return;
        Tier tier = new Tier();
        tier.name = "IT Jira";
        tier.id = 1;
        tier.appName = "10911";

        configuration.addCMDBTable(
                EntityType.Tier.name(),
                true,
                "https://apis.intel.com/itsm/api/now/table/cmdb_ci_service_discovered",
                "name",
                "u_application_id",
                "u_tier_criticality,install_status,u_application_support_org,u_application_service,u_application_service_offering,sys_updated_on,type,number,used_for,u_app_environment,owned_by,operational_status,business_unit,version,bucket,service_status,short_description,busines_criticality,u_application_url,sys_class_name,u_keyword,aliases,sys_id,comments,service_classification",
                -1,
                true);

        Entity entity = configuration.getCmdbClient().query(tier);
        assertTrue(entity != null);
        System.out.println(gson.toJson(entity));
    }

    @Test
    void queryBusinessTransaction () throws CMDBException {
        if( configuration == null ) return;
        BusinessTransaction businessTransaction = new BusinessTransaction();
        businessTransaction.name = "IT Jira";
        businessTransaction.id = 1;
        Entity entity = configuration.getCmdbClient().query(businessTransaction);
        assertTrue(entity == null);
    }

    /*
    @Test
    void testNegativeTesting () throws CMDBException { //this can't happen anymore, need to figure out another bad test
        assertThrows(IllegalArgumentException.class, () -> {
            //Entity entity = configuration.getCmdbClient().query(EntityType.valueOf("somethingVeryWrong"), "IT Jira", 1);
        });
    }
     */

    /* TODO complex tables will be for another day
    @Test
    void queryNode () throws CMDBException {
        Entity entity = configuration.getCmdbClient().query("Node", "IT Jira", 1);
        assertTrue(entity != null);
        System.out.println(gson.toJson(entity));
    }

     */
}