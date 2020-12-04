package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.catalina.Engine;
import com.cs.tomcat.catalina.Host;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Xml解析工具类
 */
public class ServerXMLUtil {

    /**
     * @return XML里的所有context
     */
    public static List<Context> getContexts() {
        List<Context> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);

        //elements that match the query "Context"
        Elements es = d.select("Context");
        for (Element e : es) {
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            Context context = new Context(path, docBase);
            result.add(context);
        }
        return result;
    }


    public static String getHostName() {
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);
        Element es = d.select("Host").first();
        return es.attr("name");
    }

    public static String getEngineDefaultHost() {
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);
        Element engine = d.select("Engine").first();
        return engine.attr("defaultHost");
    }

    public static List<Host> getHosts(Engine engine) {
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);
        Elements elements = d.select("Host");
        for (Element element : elements) {
            String name = element.attr("name");
            Host host = new Host(name, engine);
            result.add(host);
        }
        return result;
    }

    public static String getServiceName() {
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);
        Element engine = d.select("Service").first();
        return engine.attr("name");
    }


}
