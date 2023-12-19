package com.appdynamics.config;

import com.appdynamics.cmdb.EntityType;

public interface ITable {
    int getCacheTimeoutMinutes ();

    String getIdentifyingSysParm ();

    EntityType getType ();

    boolean isEnabled();

    String getSysParamsToPopulate ();

    String getParentIdentifyingSysParm ();

    String getTableURL ();
}
