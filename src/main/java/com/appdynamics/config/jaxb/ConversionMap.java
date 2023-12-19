package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConversionMap {
    @XmlElement(name = "Identity")
    private List<Identity> identities = new ArrayList<>();

    @XmlElement(name = "Entry")
    private List<Entry> entries = new ArrayList<>();

    public List<Identity> getIdentities () {
        return identities;
    }

    public void setIdentities (List<Identity> identities) {
        this.identities = identities;
    }

    public List<Entry> getEntries () {
        return entries;
    }

    public void setEntries (List<Entry> entries) {
        this.entries = entries;
    }
}

