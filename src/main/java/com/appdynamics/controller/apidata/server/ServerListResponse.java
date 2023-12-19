package com.appdynamics.controller.apidata.server;

import java.util.ArrayList;
import java.util.List;

/*
{"machineKeys":[{"machineId":1,"serverName":"SMEUTILUBU2"},{"machineId":2,"serverName":"appd-win"}],"simEnabledMachineExists":true}
 */
public class ServerListResponse {
    public List<Server> machineKeys;
    public boolean simEnabledMachineExists;

    public List<String> getServerList () {
        List<String> list = new ArrayList<>();
        for( Server machineKey : machineKeys)
            list.add(machineKey.serverName);
        return list;
    }
}
