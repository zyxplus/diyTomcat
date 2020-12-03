package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
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
    public static List<Context> getContext() {
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


}
