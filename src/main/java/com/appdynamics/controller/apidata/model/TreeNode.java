package com.appdynamics.controller.apidata.model;

public class TreeNode {
    public String name, type;
    public boolean isFolder() { return "folder".equals(type);}
    public boolean isMetric() { return !isFolder(); }
}
