package com.appdynamics.cmdb.authentication;

import com.appdynamics.exceptions.CMDBException;

public interface IOAuthClient {
    String getAccessToken () throws CMDBException;
}
