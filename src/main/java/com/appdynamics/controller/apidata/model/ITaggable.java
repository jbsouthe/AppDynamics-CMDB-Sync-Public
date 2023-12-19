package com.appdynamics.controller.apidata.model;

import com.appdynamics.cmdb.EntityType;

public interface ITaggable {
    public EntityType getEntityType();
    public String getParentName();
    public String getName ();

    public long getId ();
    public default String toKey() {
        return getEntityType()+":"+getParentName()+":"+getName()+":"+getId();
    }
}
