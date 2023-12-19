package com.appdynamics.controller.apidata.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Model {
    protected static final Logger logger = LogManager.getFormatterLogger();

    private ApplicationListing applicationListing;
    public Model( ApplicationListing applicationListing ) {
        this.applicationListing = applicationListing;
    }
    public Application getApplication( String name ) {
        for( Application application : applicationListing.getApplications() )
            if( application.name.equals(name)) return application;
        return null;
    }

    public List<Application> getSyntheticApplications() {
        return this.applicationListing.eumWebApplications;
    }

    public List<Application> getApplications() { return applicationListing.getApplications(); }
    public List<Application> getAPMApplications() { return applicationListing.apmApplications; }

    public void addApplication(Application application) {
        applicationListing.apmApplications.add(application);
        applicationListing._allApplications.add(application);
    }

    public void removeAllAppsBut(Set<String> applicationsToBuildModelsFrom) {
        logger.trace("Pruning applications to retain only %d applications: %s", applicationsToBuildModelsFrom.size(), applicationsToBuildModelsFrom);
        List<String> toRemoveList = new LinkedList<>();
        for( Application application : applicationListing.getApplications() ) {
            if( !applicationsToBuildModelsFrom.contains(application.getName()) ) {
                toRemoveList.add(application.name);
                logger.trace("Flagging for removal, application name: '%s'",application.getName() );
            } else {
                logger.trace("Keeping application name: '%s'",application.getName() );
            }
        }
        applicationListing.removeAll( toRemoveList );
        logger.trace("Removing %d applications, to leave %d applications in the model", toRemoveList.size(), applicationListing._allApplications.size());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(String.format("Model with %d Applications:",getApplications().size()));
        for(Application application : getApplications() ) {
            stringBuilder.append("\n").append(application.toString());
        }
        return stringBuilder.toString();
    }

    public Application getSyntheticApplication (String appName) {
        for( Application application : getSyntheticApplications() )
            if( application.name.equalsIgnoreCase(appName))
                return application;
        return null;
    }
}
