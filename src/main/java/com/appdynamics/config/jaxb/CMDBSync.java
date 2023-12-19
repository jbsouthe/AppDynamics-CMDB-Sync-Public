package com.appdynamics.config.jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "CMDBSync")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDBSync {
    @XmlElement(name = "SyncFrequencyMinutes")
    private int syncFrequencyMinutes = 20;

    @XmlElement(name = "CacheTimeoutMinutes")
    private int cacheTimeoutMinutes = 20;

    @XmlElement(name = "NumberOfExecuteThreads")
    private int numberOfExecuteThreads = 15;

    @XmlElement(name = "MaxBatchRetryAttempts")
    private int maxBatchRetryAttempts = 3;

    @XmlElement(name = "AES256")
    private String aes256;

    @XmlElement(name = "OAuth")
    private OAuth oauth;

    @XmlElementWrapper(name = "Tables")
    @XmlElement(name = "Table")
    private List<Table> tables = new ArrayList<>();

    @XmlElement(name = "Controller")
    private Controller controller = new Controller();

    @XmlElement(name = "ConversionMap")
    private ConversionMap conversionMap = new ConversionMap();

    public int getSyncFrequencyMinutes () {
        return syncFrequencyMinutes;
    }

    public void setSyncFrequencyMinutes (int syncFrequencyMinutes) {
        this.syncFrequencyMinutes = syncFrequencyMinutes;
    }

    public int getCacheTimeoutMinutes () {
        return cacheTimeoutMinutes;
    }

    public void setCacheTimeoutMinutes (int cacheTimeoutMinutes) {
        this.cacheTimeoutMinutes = cacheTimeoutMinutes;
    }

    public int getNumberOfExecuteThreads () {
        return numberOfExecuteThreads;
    }

    public void setNumberOfExecuteThreads (int numberOfExecuteThreads) {
        this.numberOfExecuteThreads = numberOfExecuteThreads;
    }

    public String getAes256 () {
        return aes256;
    }

    public void setAes256 (String aes256) {
        this.aes256 = aes256;
    }

    public OAuth getOauth () {
        return oauth;
    }

    public void setOauth (OAuth oauth) {
        this.oauth = oauth;
    }

    public List<Table> getTables () {
        return tables;
    }

    public void setTables (List<Table> tables) {
        this.tables = tables;
    }

    public Controller getController () {
        return controller;
    }

    public void setController (Controller controller) {
        this.controller = controller;
    }

    public ConversionMap getConversionMap () {
        return conversionMap;
    }

    public void setConversionMap (ConversionMap conversionMap) {
        this.conversionMap = conversionMap;
    }

    public int getMaxBatchRetryAttempts () {
        return maxBatchRetryAttempts;
    }

    public void setMaxBatchRetryAttempts (int maxBatchRetryAttempts) {
        this.maxBatchRetryAttempts = maxBatchRetryAttempts;
    }
}

