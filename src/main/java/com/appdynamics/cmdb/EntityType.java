package com.appdynamics.cmdb;

public enum EntityType {
    Server,
    Application,
    Tier,
    Node,
    Machine,
    BusinessTransaction,
    SyntheticPage;

    public static EntityType valueOfIgnoreCase(String name) throws IllegalArgumentException {
        for (EntityType type : EntityType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type unknown: "+ name);
    }

    public String convertToAPIEntityType() {
        switch(this) {
            case Server -> {
                return "SIM_MACHINE";
            }
            case Application -> {
                return "APPLICATION";
            }
            case Tier -> {
                return "APPLICATION_COMPONENT";
            }
            case Node -> {
                return "APPLICATION_COMPONENT_NODE";
            }
            case BusinessTransaction -> {
                return "BUSINESS_TRANSACTION";
            }
            case SyntheticPage -> {
                return "BASE_PAGE";
            }
            default -> throw new IllegalArgumentException("EntityType has new entities but the BatchTaggingRequest Constructor was not updated");
        }
    }
}
