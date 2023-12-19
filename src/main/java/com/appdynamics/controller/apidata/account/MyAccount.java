package com.appdynamics.controller.apidata.account;
/*
{"id": "23511","name": "southerland-test",
    "links": [
        {"href": "http://southerland-test.saas.appdynamics.com/controller/api/accounts/23511/applications","name": "applications"},
        {"href": "http://southerland-test.saas.appdynamics.com/controller/api/accounts/23511/licensemodules","name": "licensemodules"},
        {"href": "http://southerland-test.saas.appdynamics.com/controller/api/accounts/23511/users","name": "users"},
        {"href": "http://southerland-test.saas.appdynamics.com/controller/api/accounts/23511/apikey","name": "apikey"},
        {"href": "http://southerland-test.saas.appdynamics.com/controller/api/accounts/23511/access_token","name": "access_token"}
    ]
}
 */
public class MyAccount {
    public String id, name;
    public Link[] links;

    public class Link {
        public String href, name;
    }
}
