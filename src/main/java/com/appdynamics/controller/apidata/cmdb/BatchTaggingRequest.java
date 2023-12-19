package com.appdynamics.controller.apidata.cmdb;

import com.appdynamics.cmdb.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchTaggingRequest {
    public String entityType;
    public String source = "CMDB";
    public List<Entity> entities;
    public transient EntityType cmdbType;
    public transient int retries = 0;

    public BatchTaggingRequest( EntityType type ) {
        entityType = type.convertToAPIEntityType();
        this.entities = new ArrayList<>();
    }

    public BatchTaggingRequest( EntityType type, Entity entity ) {
        this(type);
        this.entities.add(entity);
    }

    public void addEntity (String entityName, Long entityId, Map<String, String> tags) {
        Entity entity = new Entity(entityName, entityId);
        for( String key : tags.keySet())
            entity.tags.add(new Tag(key, tags.get(key)));
        this.entities.add(entity);
    }

}
