package com.cs.tomcat;

import com.cs.tomcat.catalina.Server;

public class Bootstrap {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

}
