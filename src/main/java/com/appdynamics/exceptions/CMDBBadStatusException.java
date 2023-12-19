package com.appdynamics.exceptions;

import java.io.IOException;

public class CMDBBadStatusException extends IOException {
    public String urlRequestString, responseJSON;
    public CMDBBadStatusException (String message, String json, String url) {
        super(message);
        this.responseJSON = json;
        this.urlRequestString = url;
    }

    public void setURL(String uri) {
        this.urlRequestString=uri;
    }
}
