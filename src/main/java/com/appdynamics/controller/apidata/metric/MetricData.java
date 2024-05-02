package com.appdynamics.controller.apidata.metric;

import com.appdynamics.controller.ControllerService;
import com.appdynamics.controller.apidata.model.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MetricData {
    private static final Logger logger = LogManager.getFormatterLogger();
    public MetricData() {} //for GSON.fromJSON


    public long metricId;
    public String metricName, metricPath, frequency, hostname;
    public List<MetricValue> metricValues;
    public transient ControllerService controller;
    public transient Application application;

    public class MetricValue {
        public long startTimeInMillis, occurrences, current, min, max, count, sum, value;
        public boolean useRange;
        public double standardDeviation;
    }

    public String toString() {
        return String.format("metric: %s(%d) path: %s metrics: %d",metricName, metricId, metricPath, (metricValues==null ? 0 : metricValues.size()) );
    }
}
