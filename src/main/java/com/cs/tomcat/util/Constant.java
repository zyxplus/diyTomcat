package com.cs.tomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Constant {

    public final static String RESPONSE_HEAD_202 = "HTTP/1.1 200 OK\r\n"+"Content-Type:{}\r\n\r\n";

    public final static File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");
    public final static File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");

    public final static File CONF_FOLDER = new File(SystemUtil.get("user.dir"), "conf");
    public final static File SERVER_XML_FILE = new File(CONF_FOLDER, "server.xml");
}
