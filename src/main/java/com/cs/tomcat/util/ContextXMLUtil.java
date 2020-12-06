package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ContextXMLUtil {

    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(Constant.CONTEXT_XML_FILE);
            Document document = Jsoup.parse(xml);
            Element e = document.select("WatchedResource").first();
            return e.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }

}
