package com.appdynamics.controller.apidata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationListing implements Serializable {
    public Application analyticsApplication, cloudMonitoringApplication, dbMonApplication, simApplication;
    public List<Application> apiMonitoringApplications, apmApplications, eumWebApplications, iotApplications, mobileAppContainers;
    public List<Application> _allApplications;

    public List<Application> getApplications() {
        if( _allApplications == null ) {
            _allApplications = new ArrayList<>();
            if( analyticsApplication != null ) _allApplications.add(analyticsApplication);
            if( apiMonitoringApplications != null ) _allApplications.addAll(apiMonitoringApplications);
            if( apmApplications != null ) _allApplications.addAll(apmApplications);
            if( cloudMonitoringApplication != null ) _allApplications.add(cloudMonitoringApplication);
            if( dbMonApplication != null ) _allApplications.add(dbMonApplication);
            if( eumWebApplications != null ) _allApplications.addAll(eumWebApplications);
            if( mobileAppContainers != null ) _allApplications.addAll(mobileAppContainers);
            if( simApplication != null ) _allApplications.add(simApplication);
            if( iotApplications != null ) _allApplications.addAll(iotApplications);
        }
        return _allApplications;
    }

    public void removeAll( List<String> names ) {
        if( _allApplications == null ) getApplications();
        for(Iterator<Application> it = _allApplications.iterator(); it.hasNext(); ) {
            Application curApplication = it.next();
            if( names.contains(curApplication.getName()) )
                it.remove();
        }
    }
}
