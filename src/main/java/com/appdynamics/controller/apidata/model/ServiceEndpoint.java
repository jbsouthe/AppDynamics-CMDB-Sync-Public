package com.appdynamics.controller.apidata.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
POST /controller/restui/serviceEndpoint/list
    {"requestFilter":{"queryParams":{"applicationId":377649,"mode":"FILTER_EXCLUDED"},
    "searchText":"",
    "filters":{"sepPerfData":{"responseTime":0,"callsPerMinute":0},"type":[],"sepName":[]}},
    "columnSorts":[{"column":"NAME","direction":"ASC"}],
    "timeRangeStart":1646751701184,
    "timeRangeEnd":1646755301184}


{
  "data" : [ {
    "id" : 35526413,
    "name" : "/FISERVLET/fihttp",
    "type" : "SERVLET",
    "applicationComponent" : {
      "id" : 604042,
      "name" : "FAS"
    }
  }, {
    "id" : 31664246,
    "name" : "/adminext/actuator",
    "type" : "SERVLET",
    "applicationComponent" : {
      "id" : 585709,
      "name" : "AdminExt"
    }
  },... ] }

 */
public class ServiceEndpoint implements Serializable {
    public String name, type;
    public Tier applicationComponent;
    public long id;

    public ServiceEndpoint() {}

    public ServiceEndpoint(ResultSet resultSet, Tier tier) throws SQLException {
        name = resultSet.getString("se_name");
        id = resultSet.getLong("se_id");
        type = resultSet.getString("se_type");
        applicationComponent = tier;
    }
}
