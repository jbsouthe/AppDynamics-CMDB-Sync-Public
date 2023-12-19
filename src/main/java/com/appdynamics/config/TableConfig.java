package com.appdynamics.config;

import com.appdynamics.cmdb.EntityType;

public class TableConfig implements ITable {
    private EntityType type;
    private String tableURL;
    private String identifyingSysParm = "name";
    private String parentIdentifyingSysParm = null;
    private String sysParamsToPopulate = null;
    private boolean enabled = true;
    private int cacheTimeoutMinutes = 20;

    public TableConfig (EntityType type, Boolean enabled, String tableURL, String identifyingSysParm, String parentIdentifyingSysParm, String sysParamsToPopulate, Integer cacheTimeoutMinutes) {
        this.type = type;
        this.tableURL=tableURL;
        if( identifyingSysParm != null )
            this.identifyingSysParm=identifyingSysParm;
        this.parentIdentifyingSysParm = parentIdentifyingSysParm;
        if( sysParamsToPopulate != null && !sysParamsToPopulate.equals("*"))
            this.sysParamsToPopulate=sysParamsToPopulate;
        this.enabled=enabled;
        this.cacheTimeoutMinutes = cacheTimeoutMinutes;
    }

    public EntityType getType () {
        return type;
    }

    public String getTableURL () {
        return tableURL;
    }

    public String getIdentifyingSysParm () {
        return identifyingSysParm;
    }

    public String getParentIdentifyingSysParm () { return parentIdentifyingSysParm; }

    public String getSysParamsToPopulate () {
        return sysParamsToPopulate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getCacheTimeoutMinutes () {
        return cacheTimeoutMinutes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("Table type: %s enabled: %s identifier: '%s' parent: '%s' sysparms: '%s'", type, enabled, identifyingSysParm, parentIdentifyingSysParm, sysParamsToPopulate));
        return sb.toString();
    }
}
