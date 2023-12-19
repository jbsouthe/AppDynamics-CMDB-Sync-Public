package com.appdynamics.cmdb.converter;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.config.Configuration;
import com.appdynamics.config.ITable;
import com.appdynamics.config.EntityConverter;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.Tier;
import com.appdynamics.exceptions.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityConverterTest {

    @Test
    void testSimpleConversionQuery () throws InvalidConfigurationException {
        EntityConverter mapEntry = new EntityConverter("Application", "AppDName", "CMDBName");
        System.out.println("key: "+ mapEntry.getKey());
        assertEquals("AppDName:Application", mapEntry.getKey());

        Configuration configuration = new Configuration();
        configuration.addCMDBTable(EntityType.Application.name(), true, "http://localhost/", "name", null,"owner,contact,location", -1);
        ITable table = configuration.getTableConfig(EntityType.Application);

        Application application = new Application("AppDName");
        assertEquals(EntityConverter.makeKey(table,application), mapEntry.getKey());

        assertEquals("name=CMDBName", mapEntry.getQuery(table, application));
    }

    @Test
    void testComplexConversionQuery () throws InvalidConfigurationException {
        EntityConverter mapEntry = new EntityConverter("Tier", "AppDTier", "CMDBTier", "Application", "AppDName", "CMDBName");
        System.out.println("key: "+ mapEntry.getKey());
        assertEquals("AppDTier:Tier", mapEntry.getKey());

        Configuration configuration = new Configuration();
        configuration.addCMDBTable(EntityType.Tier.name(), true, "http://localhost/", "name", "parent","owner,contact,location", -1);
        configuration.addCMDBTable(EntityType.Application.name(), true, "http://localhost/", "name", null,"owner,contact,location", -1);
        ITable table = configuration.getTableConfig(EntityType.Tier);

        Application application = new Application("AppDName");
        Tier tier = new Tier("AppDTier");
        assertEquals(EntityConverter.makeKey(table,tier), mapEntry.getKey());

        assertEquals("name=CMDBTier^parent=CMDBName", mapEntry.getQuery(table, application));
    }
}