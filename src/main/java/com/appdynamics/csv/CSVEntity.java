package com.appdynamics.csv;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.cmdb.Tag;

import java.util.HashMap;
import java.util.Map;

public class CSVEntity {
    public String appName;
    public String name;
    public EntityType type;
    public Map<String,String> tags;

    public CSVEntity(String appName, Entity entity, EntityType entityType) {
        this.appName = appName;
        this.name = entity.entityName;
        this.type = entityType;
        this.tags = new HashMap<>();
        for (Tag tag : entity.tags) {
            this.tags.put(tag.key, tag.value);
        }
    }
}
