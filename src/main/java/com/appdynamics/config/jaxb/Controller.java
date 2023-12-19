package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Controller {
    @XmlElement(name = "URL")
    private String url;

    @XmlElement(name = "ClientID")
    private String clientId;

    @XmlElement(name = "ClientSecret")
    private ClientSecret clientSecret;

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public String getClientId () {
        return clientId;
    }

    public void setClientId (String clientId) {
        this.clientId = clientId;
    }

    public ClientSecret getClientSecret () {
        return clientSecret;
    }

    public void setClientSecret (ClientSecret clientSecret) {
        this.clientSecret = clientSecret;
    }

}

