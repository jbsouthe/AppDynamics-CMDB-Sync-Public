package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Table {
    @XmlAttribute
    private String type;

    @XmlAttribute
    private boolean enabled=true;

    @XmlElement(name = "URL")
    private String url;

    @XmlElement(name = "IdentifyingSysParm")
    private String identifyingSysParm;

    @XmlElement(name = "ParentIdentifyingSysParm")
    private String parentIdentifyingSysParm;

    @XmlElement(name = "SysParms")
    private String sysParms;

    @XmlElement(name = "CacheTimeoutMinutes")
    private int cacheTimeoutMinutes = -1;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public boolean isEnabled () {
        return enabled;
    }

    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public String getIdentifyingSysParm () {
        return identifyingSysParm;
    }

    public void setIdentifyingSysParm (String identifyingSysParm) {
        this.identifyingSysParm = identifyingSysParm;
    }

    public String getParentIdentifyingSysParm () {
        return parentIdentifyingSysParm;
    }

    public void setParentIdentifyingSysParm (String parentIdentifyingSysParm) {
        this.parentIdentifyingSysParm = parentIdentifyingSysParm;
    }

    public String getSysParms () {
        return sysParms;
    }

    public void setSysParms (String sysParms) {
        this.sysParms = sysParms;
    }

    public int getCacheTimeoutMinutes () {
        return cacheTimeoutMinutes;
    }

    public void setCacheTimeoutMinutes (int cacheTimeoutMinutes) {
        this.cacheTimeoutMinutes = cacheTimeoutMinutes;
    }

    // getters and setters
}

