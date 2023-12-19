package com.appdynamics.controller.apidata.model;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.Controller;
import com.appdynamics.controller.apidata.metric.MetricData;
import com.appdynamics.exceptions.ControllerBadStatusException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Application implements Comparable<Application>, ITaggable {
    private static final Logger logger = LogManager.getFormatterLogger();
    public String name, accountGuid;
    public long id;
    public boolean active;
    public List<Tier> tiers = new ArrayList<>();
    public List<Node> nodes = new ArrayList<>();
    public List<BusinessTransaction> businessTransactions = new ArrayList<>();
    public List<Backend> backends = new ArrayList<>();
    public List<ServiceEndpoint> serviceEndpoints = new ArrayList<>();
    private transient Controller controller;
    private transient Map<String,MetricData> controllerMetricMap = new HashMap<>();
    private boolean finishedInitialization=false;

    public Application() {} //for GSON

    public Application( String name ) {
        this.name = name;
    }

    public String toString() {
        return String.format("Application: %s(%d) Tiers: %d Nodes: %d Business Transactions: %d Backends: %d Service Endpoints: %d", name, id, tiers.size(), nodes.size(), businessTransactions.size(), backends.size(), serviceEndpoints.size());
    }

    public void setController( Controller controller ) {
        this.controller=controller;
        this.finishedInitialization=false;
        init();
    }

    public Set<String> getMetricNames() {
        init();
        return controllerMetricMap.keySet();
    }

    public Tier getTier( String name ) {
        init();
        for( Tier tier : tiers )
            if( tier.name.equals(name) ) return tier;
        return null;
    }

    public Tier getTier( Tier o ) {
        init();
        for( Tier tier : tiers )
            if( tier.equals(o) ) return tier;
        return null;
    }

    public Tier getTier( long id ) {
        init();
        for( Tier tier : tiers )
            if( tier.id == id ) return tier;
        return null;
    }

    public Node getNode( String name ) {
        init();
        for( Node node : nodes )
            if( node.name.equals(name) ) return node;
        return null;
    }

    public Node getNode( Node o ) {
        init();
        for( Node node : nodes )
            if( node.equals(o) ) return node;
        return null;
    }

    public Node getNode( long id ) {
        init();
        for( Node node : nodes )
            if( node.id == id ) return node;
        return null;
    }

    public BusinessTransaction getBusinessTransaction( String name ) {
        init();
        for( BusinessTransaction businessTransaction : businessTransactions )
            if( businessTransaction != null && businessTransaction.name != null && businessTransaction.name.equals(name) ) return businessTransaction;
        return null;
    }

    public BusinessTransaction getBusinessTransaction( BusinessTransaction o ) {
        init();
        for( BusinessTransaction businessTransaction : businessTransactions )
            if( businessTransaction.equals(o) ) return businessTransaction;
        return null;
    }

    public BusinessTransaction getBusinessTransaction(Long btId) {
        init();
        for( BusinessTransaction businessTransaction : businessTransactions )
            if( businessTransaction.id == btId ) return businessTransaction;
        return null;
    }

    public Backend getBackend( String name ) {
        init();
        for( Backend backend : backends )
            if( backend.name.equals(name) ) return backend;
        return null;
    }

    public Backend getBackend( Backend o ) {
        init();
        for( Backend backend : backends )
            if( backend.equals(o) ) return backend;
        return null;
    }

    @Override
    public int compareTo( Application o ) {
        if( o == null ) return 1;
        if( o.name.equals(name) ) return 0;
        return -1;
    }

    public boolean equals( Application o ) {
        return compareTo(o) == 0;
    }

    public boolean isFinishedInitialization() { return finishedInitialization; }
    public synchronized void init() {
        if( isControllerNull() ) return;
        if( !isFinishedInitialization() ) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = null;
            try {
                json = controller.getRequest("controller/rest/applications/%d/tiers?output=JSON", id);
                Tier[] tiersList = gson.fromJson(json, Tier[].class);
                if (tiersList != null) {
                    for (Tier tier : tiersList) {
                        tier.appName = this.name;
                        tiers.add(tier);
                    }
                    logger.info("Added %d Tiers to Application: %s", tiersList.length, name);
                }
            } catch (ControllerBadStatusException controllerBadStatusException ) {
                logger.error("Error initializing Application %s, message: %s", name, controllerBadStatusException.getMessage());
            }
            try {
                json = controller.getRequest("controller/rest/applications/%d/nodes?output=JSON", id);
                Node[] nodesList = gson.fromJson(json, Node[].class);
                if( nodesList != null ) {
                    for (Node node : nodesList)
                        nodes.add(node);
                    logger.info("Added %d Nodes to Application: %s", nodesList.length, name);
                }
            } catch (ControllerBadStatusException controllerBadStatusException ) {
                logger.error("Error initializing Application %s, message: %s", name, controllerBadStatusException.getMessage());
            }
            try {
                json = controller.getRequest("controller/rest/applications/%d/business-transactions?output=JSON", id);
                BusinessTransaction[] businessTransactionsList = gson.fromJson(json, BusinessTransaction[].class);
                if (businessTransactionsList != null) {
                    for (BusinessTransaction businessTransaction : businessTransactionsList)
                        businessTransactions.add(businessTransaction);
                    logger.info("Added %d Business Transactions to Application: %s", businessTransactionsList.length, name);
                }
            } catch (ControllerBadStatusException controllerBadStatusException ) {
                logger.error("Error initializing Application %s, message: %s", name, controllerBadStatusException.getMessage());
            }
            try {
                json = controller.getRequest("controller/rest/applications/%d/backends?output=JSON", id);
                Backend[] backendsList = gson.fromJson(json, Backend[].class);
                if (backendsList != null) {
                    for (Backend backend : backendsList)
                        backends.add(backend);
                    logger.info("Added %d Backends to Application: %s", backendsList.length, name);
                }
            } catch (ControllerBadStatusException controllerBadStatusException ) {
                logger.error("Error initializing Application %s, message: %s", name, controllerBadStatusException.getMessage());
            }
            try {
                serviceEndpoints.addAll(controller.getServiceEndpoints(id));
            } catch (ControllerBadStatusException controllerBadStatusException ) {
                logger.error("Error initializing Application %s, message: %s", name, controllerBadStatusException.getMessage());
            }
            logger.info("Added %d Service Endpoints to Application: %s", serviceEndpoints.size(), name);
            synchronized (this.controllerMetricMap) {
                if( isFinishedInitialization() ) return; //only let the first one run, all others return quickly once unblocked
                //do anything more here, before setting to initialized
                this.finishedInitialization = true; //setting this here because we want to continue, even if partial data
            }
        }
    }

    private void findMetrics(Controller controller, TreeNode[] somethings, String path) {
        if( somethings == null || somethings.length == 0 ) return;
        if( !"".equals(path) ) path += "|";

        for( TreeNode something : somethings ) {
            if( something.isFolder() ) {
                findMetrics( controller, controller.getApplicationMetricFolders(this, path+something.name), path+something.name);
            } else if( "Custom Metrics".contains(path + something.name)) {
                logger.debug("Adding metric: %s%s",path,something.name);
                controllerMetricMap.put(path+something.name, null);
            }
        }
    }
    public String getName() { return this.name; }

    @Override
    public long getId () {
        return id;
    }

    public boolean isControllerNull() { return controller == null; }

    public ServiceEndpoint getServiceEndpoint(Long serviceEndpointId) {
        for( ServiceEndpoint serviceEndpoint : serviceEndpoints )
            if( serviceEndpoint.id == serviceEndpointId ) return serviceEndpoint;
        return null;
    }

    public ServiceEndpoint getServiceEndpoint(ServiceEndpoint sourceServiceEndpoint) {
        return getServiceEndpoint(sourceServiceEndpoint.name, sourceServiceEndpoint.applicationComponent.name);
    }

    public ServiceEndpoint getServiceEndpoint(String serviceEndpointName, String componentName) {
        for( ServiceEndpoint serviceEndpoint : serviceEndpoints ) {
            if( serviceEndpoint.name.equals(serviceEndpointName)
                    && serviceEndpoint.applicationComponent.name.equals(componentName))
                return serviceEndpoint;
        }
        return null;
    }

    @Override
    public EntityType getEntityType () {
        return EntityType.Application;
    }

    @Override
    public String getParentName () {
        return null;
    }

}
