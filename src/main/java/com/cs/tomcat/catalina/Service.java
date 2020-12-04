package com.cs.tomcat.catalina;

import com.cs.tomcat.util.ServerXMLUtil;

public class Service {
    private String name;
    private Engine engine;

    public Service() {
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine();
    }

    public Engine getEngine() {
        return engine;
    }

}
