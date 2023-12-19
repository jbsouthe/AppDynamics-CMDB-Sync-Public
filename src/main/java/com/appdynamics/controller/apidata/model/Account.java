package com.appdynamics.controller.apidata.model;

import java.util.ArrayList;
import java.util.List;

public class Account implements Comparable<Account> {
    public String name;
    public long id;
    public List<Application> applications = new ArrayList<>();

    public Account() {}
    public Account( long id, String name ) {
        this.id=id;
        this.name=name;
    }

    public Application getApplication( String name ) {
        for( Application app : applications )
            if( app.name.equals(name)) return app;
        return null;
    }

    public Application getApplication( long id ) {
        for( Application app : applications )
            if( app.id == id ) return app;
        return null;
    }

    @Override
    public int compareTo( Account o ) {
        if( o==null) return -1;
        if( o.name.equals(name) ) return 0;
        return -1;
    }

    public boolean equals( Account o ) {
        return compareTo(o) == 0;
    }
}
