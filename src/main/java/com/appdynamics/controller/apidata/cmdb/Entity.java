package com.appdynamics.controller.apidata.cmdb;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    public String entityName;
    public Long entityId;
    public List<Tag> tags;

    public Entity (String name, long id) {
        this.entityName = name;
        this.entityId = id;
        this.tags = new ArrayList<>();
    }
}
