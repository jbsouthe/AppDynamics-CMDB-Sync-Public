package com.appdynamics.controller.apidata.synthetic.page;

public class RequestFilter {
    //{"applicationId":50,"fetchSyntheticData":true}
    public long applicationId;
    public boolean fetchSyntheticData;
    public RequestFilter( long applicationId, boolean fetchSyntheticData ) {
        this.applicationId = applicationId;
        this.fetchSyntheticData = fetchSyntheticData;
    }
}
