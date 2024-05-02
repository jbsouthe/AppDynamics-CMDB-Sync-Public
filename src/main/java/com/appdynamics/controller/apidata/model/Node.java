package com.appdynamics.controller.apidata.model;

import com.appdynamics.cmdb.EntityType;

import java.io.Serializable;

public class Node implements Comparable<Node>, ITaggable, Serializable {
    public String name, type, tierName, machineName, machineOSType, machineAgentVersion, appAgentVersion, agentType;
    public long id, tierId, machineId;
    public boolean machineAgentPresent, appAgentPresent;

    @Override
    public int compareTo( Node o ) {
        if( o==null) return -1;
        if( o.name.equals(name) && o.tierName.equals(tierName)  ) return 0;
        return -1;
    }

    public boolean equals( Node o ) {
        return compareTo(o) == 0;
    }

    @Override
    public EntityType getEntityType () {
        return EntityType.Node;
    }

    @Override
    public String getParentName () {
        return tierName;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public long getId () {
        return id;
    }

    public String toString() {
        return String.format("Node: %s(%d) of Tier: '%s'(%d) on Machine id: (%d)", name, id, tierName, tierId, machineId);
    }
}
