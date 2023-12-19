package com.appdynamics.controller.apidata.server;

/*
{ "filter":
    { "appIds":[],"nodeIds":[],"tierIds":[],"types":["PHYSICAL","CONTAINER_AWARE"],"timeRangeStart":1692629938801,"timeRangeEnd":1692633538801},
    "sorter":{"field":"HEALTH","direction":"ASC"}
}
 */
public class ServerListRequest {
    public Filter filter = new Filter();
    public Sorter sorter = new Sorter();
}
