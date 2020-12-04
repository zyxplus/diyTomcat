package com.cs.tomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.util.ServerXMLUtil;

import java.util.List;

public class Service {
    private String name;
    private Engine engine;
    private Server server;
    private List<Connector> connectors;

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

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();
        for (Connector connector : connectors) {
            //初始化
            connector.init();
        }
        LogFactory.get().info("Initialization processed in {} ms", timeInterval.intervalMs());
        for (Connector connector : connectors) {
            //开始运行
            connector.start();
        }
    }

    public void start() {
        init();
    }

}
