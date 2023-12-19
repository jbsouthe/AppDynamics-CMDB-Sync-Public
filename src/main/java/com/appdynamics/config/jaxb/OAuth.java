package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class OAuth {
    @XmlAttribute
    private String type;

    @XmlElement(name = "URL")
    private String url;

    @XmlElement(name = "ClientID")
    private String clientId;

    @XmlElement(name = "ClientSecret")
    private ClientSecret clientSecret;

    @XmlElement(name = "ClientScope")
    private String clientScope;

    @XmlElement(name = "UserName")
    private String userName;

    @XmlElement(name = "Password")
    private ClientSecret password;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

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

    public String getClientScope () {
        return clientScope;
    }

    public void setClientScope (String clientScope) {
        this.clientScope = clientScope;
    }

    public String getUserName () {
        return userName;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }

    public ClientSecret getPassword () {
        return password;
    }

    public void setPassword (ClientSecret password) {
        this.password = password;
    }

    // getters and setters
}

