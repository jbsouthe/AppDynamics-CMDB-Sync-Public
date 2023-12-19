package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ClientSecret {
    @XmlAttribute(name = "encrypted")
    private boolean encrypted;

    @XmlValue
    private String value;

    public boolean isEncrypted () {
        return encrypted;
    }

    public void setEncrypted (boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }
}
