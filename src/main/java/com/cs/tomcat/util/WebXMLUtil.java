package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import com.cs.tomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取context下的欢迎文件
 */
public class WebXMLUtil {

    private static Map<String, String> mimeTypeMapping = new HashMap<>();

    public static String getWelcomeFile(Context context) {
        String xml = FileUtil.readUtf8String(Constant.webXmlFile);
        Document d = Jsoup.parse(xml);
        Elements elements = d.select("welcome-file");
        for (Element element : elements) {
            String welcomeFileName = element.text();
            File file = new File(context.getDocBase(), welcomeFileName);
            if (file.exists()) {
                return file.getName();
            }
        }
        return "index.html";
    }


    /**
     * HashMap: Key-拓展名，value-互联网媒体类型
     */
    private static void initMimeType() {
        String xml = FileUtil.readUtf8String(Constant.webXmlFile);
        Document d = Jsoup.parse(xml);
        Elements elements = d.select("mime-mapping");
        for (Element element : elements) {
            String extName = element.select("extention").first().text();
            String mimeType = element.select("mime-type").first().text();
            mimeTypeMapping.put(extName, mimeType);
        }
    }

    public static synchronized String getMimeType(String extName) {
        if (mimeTypeMapping.isEmpty()) {
            initMimeType();
        }
        String mimeType = mimeTypeMapping.get(extName);
        if (null == mimeType) {
            return "text/html";
        }
        return mimeType;
    }


}
