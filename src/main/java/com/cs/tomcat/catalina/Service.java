package com.cs.tomcat.catalina;

import com.cs.tomcat.util.ServerXMLUtil;

public class Service {
    private String name;
    private Engine engine;
    private Server server;

    public Service(Server server) {
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine();
        this.server = server;
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }
}
