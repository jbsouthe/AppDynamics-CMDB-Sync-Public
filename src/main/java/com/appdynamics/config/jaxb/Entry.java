package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {
    @XmlElement(name = "Type")
    private String type;

    @XmlElement(name = "AppDynamicsName")
    private String appDynamicsName;

    @XmlElement(name = "CMDBName")
    private String cmdbName;

    @XmlElement(name = "Parent")
    private Entry parent;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getAppDynamicsName () {
        return appDynamicsName;
    }

    public void setAppDynamicsName (String appDynamicsName) {
        this.appDynamicsName = appDynamicsName;
    }

    public String getCmdbName () {
        return cmdbName;
    }

    public void setCmdbName (String cmdbName) {
        this.cmdbName = cmdbName;
    }

    public Entry getParent () {
        return parent;
    }

    public void setParent (Entry parent) {
        this.parent = parent;
    }

    // getters and setters
}
