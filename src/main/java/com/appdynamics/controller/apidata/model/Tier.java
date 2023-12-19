package com.appdynamics.controller.apidata.model;

import com.appdynamics.cmdb.EntityType;

public class Tier implements Comparable<Tier>, ITaggable {
    public String name, type, agentType, appName;
    public long id, numberOfNodes;

    public Tier(){}
    public Tier( String name ) { this.name = name; }
    @Override
    public int compareTo( Tier o ) {
        if(o==null) return -1;
        if( o.name.equals(name) ) return 0;
        return 1;
    }

    public boolean equals( Tier o ) {
        return compareTo(o) == 0;
    }

    @Override
    public EntityType getEntityType () {
        return EntityType.Tier;
    }

    @Override
    public String getParentName () {
        return appName;
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
        return String.format("Tier: %s(%d) Nodes: %d", name, id, numberOfNodes);
    }
}
