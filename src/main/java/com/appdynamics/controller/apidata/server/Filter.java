package com.appdynamics.controller.apidata.server;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    public List<String> appIds = new ArrayList<>();
    public List<String> nodeIds = new ArrayList<>();
    public List<String> tierIds = new ArrayList<>();
    public List<String> types = new ArrayList<>(){{add("PHYSICAL"); add("CONTAINER_AWARE");}};
    public long timeRangeEnd = System.currentTimeMillis();
    public long timeRangeStart = System.currentTimeMillis() - 3600000;
}
