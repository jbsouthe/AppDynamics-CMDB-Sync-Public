package com.appdynamics.csv;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.cmdb.Tag;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CSVFile {
    private final Set<String> tagHeaderSet = new HashSet<>();
    private final List<CSVEntity> entityList = new ArrayList<>();
    private final PrintWriter writer;

    public CSVFile(String csvFileName) throws IOException {
        this.writer = new PrintWriter(new FileWriter(csvFileName));
    }

    public void add(Entity entity, EntityType entityType) {
        add(null, entity, entityType);
    }

    public void add(String appName, Entity entity, EntityType entityType) {
        entityList.add(new CSVEntity(appName, entity, entityType));
        for(Tag tag : entity.tags) {
            tagHeaderSet.add(tag.key);
        }
    }

    public void close() {
        writer.print("EntityType,Name,Application");
        for( String key : tagHeaderSet) {
            writer.print("," + key);
        }
        writer.println();
        for(CSVEntity entity : entityList) {
            writer.print(String.format("%s,%s,%s",entity.type,entity.name,entity.appName));
            for(String key : tagHeaderSet) {
                writer.print("," + entity.tags.get(key));
            }
        }
    }
}
