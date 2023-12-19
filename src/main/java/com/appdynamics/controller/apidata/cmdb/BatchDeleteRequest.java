package com.appdynamics.controller.apidata.cmdb;

import com.appdynamics.cmdb.EntityType;

import java.util.ArrayList;
import java.util.List;

public class BatchDeleteRequest {
    public String entityType;
    public List<Long> entityIds;
    public BatchDeleteRequest(EntityType entityType, List<Long> ids) {
        this.entityType = entityType.convertToAPIEntityType();
        this.entityIds = new ArrayList<>();
        this.entityIds.addAll(ids);
    }
}
