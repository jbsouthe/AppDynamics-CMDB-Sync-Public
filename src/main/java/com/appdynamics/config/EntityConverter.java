package com.appdynamics.config;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.apidata.model.ITaggable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityConverter {
    private static final Logger logger = LogManager.getFormatterLogger();
    public String name, identity;
    public EntityType entityType;
    public EntityConverter parent;

    public EntityConverter (String type, String name, String identity) {
        logger.trace("EntityConverter : %s[%s] = %s",name, type,identity);
        this.entityType = EntityType.valueOfIgnoreCase(type);
        this.name = name;
        this.identity = identity;
    }

    public EntityConverter (String type, String name, String identity, String parentType, String parentAppDName, String parentCMDBName ) {
        this(type, name, identity);
        if( parentType != null && parentAppDName != null && parentCMDBName != null )
            this.parent = new EntityConverter(parentType, parentAppDName, parentCMDBName);
    }

    public static String makeKey (ITable table, ITaggable identifier) {
        return identifier.getName() +":"+ table.getType().name();
    }

    public String getKey() { return name +":"+ entityType; }

    public String getQuery (ITable table, ITaggable identifier) {
        StringBuilder query = new StringBuilder();
        query.append(table.getIdentifyingSysParm() + "=" + identity);
        if (table.getParentIdentifyingSysParm() != null) {
            if( this.parent != null ) {
                query.append("^" + table.getParentIdentifyingSysParm() + "=" + this.parent.identity);
            } else {
                query.append("^" + table.getParentIdentifyingSysParm() + "=" + identifier.getParentName());
            }
        }
        return query.toString();
    }
}
