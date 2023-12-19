package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Identity {
    @XmlAttribute
    private String type;

    @XmlAttribute
    private String from;

    @XmlAttribute
    private String to;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getFrom () {
        return from;
    }

    public void setFrom (String from) {
        this.from = from;
    }

    public String getTo () {
        return to;
    }

    public void setTo (String to) {
        this.to = to;
    }

    // getters and setters
}
