package com.appdynamics.controller.apidata.model;

import java.io.Serializable;

public class TreeNode implements Serializable {
    public String name, type;
    public boolean isFolder() { return "folder".equals(type);}
    public boolean isMetric() { return !isFolder(); }
}
