package com.cs.tomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.*;
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
    public static List<Context> getContexts(Host host) {
        List<Context> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);

        //elements that match the query "Context"
        Elements es = d.select("Context");
        for (Element e : es) {
            String path = e.attr("path");
            String docBase = e.attr("docBase");
            boolean reloadable = Convert.toBool(e.attr("docBase"), true);
            Context context = new Context(path, docBase, host, reloadable);
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
        Element e = d.select("Engine").first();
        return e.attr("defaultHost");
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
        Element e = d.select("Service").first();
        return e.attr("name");
    }

    public static List<Connector> getConnectors(Service service) {
        List<Connector> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.SERVER_XML_FILE);
        Document d = Jsoup.parse(xml);
        Elements elements = d.select("Connector");
        for (Element element : elements) {
            int port = Convert.toInt(element.attr("port"));
            String compression = element.attr("compression");
            Integer compressionMinSize = Convert.toInt(element.attr("compressionMinSize"), 0);
            String noCompressionUserAgents = element.attr("noCompressionUserAgents");
            String compressableMimeType = element.attr("compressableMimeType");
            Connector connector = new Connector(service);
            connector.setPort(port);
            connector.setCompression(compression);
            connector.setCompressableMimeType(compressableMimeType);
            connector.setNoCompressionUserAgents(noCompressionUserAgents);
            result.add(connector);
        }
        return result;
    }


}
