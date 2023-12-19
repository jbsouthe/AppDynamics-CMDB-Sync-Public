package com.appdynamics.cmdb;

import java.util.Map;

public interface IResult {
    boolean isCacheValid (int expireMinutes);

    void setKeyField (String key);
    String getKeyField();

    Map<String, String> getResult();

    boolean isEmpty ();

    boolean update (IResult result);
}
