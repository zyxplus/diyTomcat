package com.cs.tomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Constant {

    public final static String RESPONSE_HEAD_202 = "HTTP/1.1 200 OK\r\n" + "Content-Type:{}\r\n\r\n";

    public final static File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");
    public final static File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");

    public final static File CONF_FOLDER = new File(SystemUtil.get("user.dir"), "conf");
    public final static File SERVER_XML_FILE = new File(CONF_FOLDER, "server.xml");

    public static final String RESPONSE_HEAD_404 = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n\r\n";

    public static final String TEXT_FORMAT_404 =
            "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>" +
                    "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
                    "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
                    "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
                    "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
                    "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
                    "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
                    "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> " +
                    "</head><body><h1>HTTP Status 404 - {}</h1>" +
                    "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
                    "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>" +
                    "</body></html>";
}
