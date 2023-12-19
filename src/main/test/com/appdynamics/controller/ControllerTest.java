package com.appdynamics.controller;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.cmdb.Tag;
import com.appdynamics.controller.apidata.model.Model;
import com.appdynamics.controller.apidata.server.Server;
import com.appdynamics.exceptions.ControllerBadStatusException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    Configuration configuration = null;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ControllerTest() throws Exception {
        Configurator.setAllLevels("", Level.DEBUG);
        configuration = new Configuration("test-config.xml");
    }

    @Test
    void getBearerToken () {
        String token = configuration.getController().getBearerToken();
        assertTrue(token.length() > 0);
        System.out.println("Token: "+ token);
    }

    @Test
    void getModel () {
        Model model = configuration.getController().getModel(true);
        assertTrue(model.getApplications().size() > 0);
    }

    @Test
    void getServerList () throws ControllerBadStatusException {
        List<Server> servers = configuration.getController().getServerList();
        assertTrue(servers.size() > 0);
    }

    @Test
    void updateTags () throws Exception {
        /*
        Server server = new Server();
        server.serverName = "SMEUTILUBU2";
        server.machineId = 1;
        Entity appEntity = configuration.getCmdbClient().query(server);
        BatchTaggingRequest request = new BatchTaggingRequest(EntityType.Server, appEntity);
        BatchResponse response = configuration.getController().updateTags(request);
         */
        Entity entity = new Entity("ECommerce_MA", 11);
        entity.tags.add(new Tag("Owner", "Utkarsh Porwal"));
        BatchTaggingRequest batchTaggingRequest = new BatchTaggingRequest(EntityType.Server, entity);
        System.out.println("Request: " + gson.toJson(batchTaggingRequest));
        String json = configuration.getController().postRequest("controller/restui/tags/tagEntitiesInBatch", gson.toJson(batchTaggingRequest));
        System.out.println("Response: " + json);
        assertEquals("{\n" +
                "  \"entityType\" : \"SIM_MACHINE\",\n" +
                "  \"success\" : {\n" +
                "    \"count\" : 1,\n" +
                "    \"entityIds\" : [ 11 ]\n" +
                "  },\n" +
                "  \"failure\" : {\n" +
                "    \"count\" : 0,\n" +
                "    \"entityIds\" : [ ]\n" +
                "  }\n" +
                "}", json.toString());
        //gson.fromJson(json, BatchResponse.class);
    }

    /*
    @Test
    void testUnknownTagEntity () throws Exception {
        Entity entity = new Entity("This_Does_Not_Exist", 0);
        entity.tags.add(new Tag("Owner", "Utkarsh Porwal"));
        BatchTaggingRequest batchTaggingRequest = new BatchTaggingRequest(EntityType.Application, entity);
        System.out.println("Request: " + gson.toJson(batchTaggingRequest));
        String json = configuration.getController().postRequest("controller/restui/tags/tagEntitiesInBatch", gson.toJson(batchTaggingRequest));
        System.out.println("Response: " + json);
        assertEquals("{\n" +
                "  \"entityType\" : \"MACHINE_INSTANCE\",\n" +
                "  \"success\" : {\n" +
                "    \"count\" : 1,\n" +
                "    \"entityIds\" : [ 11 ]\n" +
                "  },\n" +
                "  \"failure\" : {\n" +
                "    \"count\" : 0,\n" +
                "    \"entityIds\" : [ ]\n" +
                "  }\n" +
                "}", json.toString());
        gson.fromJson(json, BatchResponse.class);
    }
    */
}