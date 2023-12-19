package com.appdynamics.controller.apidata.synthetic.page;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.apidata.model.ITaggable;

public class SyntheticPage implements ITaggable {
    public long applicationId, addId;
    public String name, internalName;
    public String type;

    @Override
    public EntityType getEntityType () {
        return EntityType.SyntheticPage;
    }

    @Override
    public String getParentName () {
        return null;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public long getId () {
        return addId;
    }

    public String toString() { return String.format("SyntheticPage: %s(%d)",getName(), getId()); }

    @Override
    public String toKey () {
        return ITaggable.super.toKey();
    }
    /**"data" : [ {
     "applicationId" : 50,
     "addId" : 72,
     "name" : "content/www/us",
     "internalName" : "content/www/us",
     "type" : "BASE_PAGE",
     "requestPerMinute" : 64,
     "totalNumberOfEndUserRequests" : 64,
     "endUserResponseTime" : 4126,
     "visuallyCompleteTime" : null,
     "pageCompleteTime" : null,
     "endUserResponseTimeP0" : null,
     "endUserResponseTimeP1" : null,
     "endUserResponseTimeP2" : null,
     "endUserResponseTimeP3" : null,
     "visuallyCompleteTimeP0" : null,
     "visuallyCompleteTimeP1" : null,
     "visuallyCompleteTimeP2" : null,
     "visuallyCompleteTimeP3" : null,
     "domReadyTime" : null,
     "frontEndTime" : null,
     "resourceFetchTime" : null,
     "htmlDownloadAndDomBuildingTime" : null,
     "htmlDownloadTime" : null,
     "ajaxResponseDownloadTime" : null,
     "domBuildingTime" : null,
     "ajaxCallBackExecutionTime" : null,
     "firstByteTime" : null,
     "responseAvailableTime" : null,
     "serverConnectionTime" : null,
     "totalPageViewWithJavaScriptError" : null,
     "pageViewWithJavaScriptErrorPerMinute" : null,
     "ajaxRequestErrorPerMinute" : null,
     "outlierRequests" : null,
     "totalOutlierRequests" : null,
     "synthetic" : true
     },
     */
}
