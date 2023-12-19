package com.appdynamics.controller.apidata.synthetic;

public class SyntheticJob {
    public ScriptConfig config;
    public String getName() { return config.description; }
    public String getId() { return config.id; }
}
