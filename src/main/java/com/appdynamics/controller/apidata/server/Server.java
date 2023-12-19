package com.appdynamics.controller.apidata.server;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.apidata.model.ITaggable;

public class Server implements ITaggable {
    public long machineId;
    public String serverName;

    @Override
    public EntityType getEntityType () {
        return EntityType.Server;
    }

    @Override
    public String getParentName () {
        return null;
    }

    @Override
    public String getName () {
        return serverName;
    }

    @Override
    public long getId () {
        return machineId;
    }

    public String toString() { return String.format("Server: %s(%d)",serverName,machineId); }
}
