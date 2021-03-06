package com.cs.tomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.catalina.classloader.WebappClassLoader;
import com.cs.tomcat.catalina.exception.WebConfigDuplicatedException;
import com.cs.tomcat.catalina.watcher.ContextFileChangeWatcher;
import com.cs.tomcat.http.ApplicationContext;
import com.cs.tomcat.http.StandardServletConfig;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ContextXMLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.lang.annotation.Documented;
import java.util.*;

public class Context {
    private String path;
    private String docBase;

    //对应WEB-INF/web.xml文件
    private File contextWebXmlFile;

    private Map<String, String> url_servletClassName;
    private Map<String, String> url_servletName;
    private Map<String, String> servletName_className;
    private Map<String, String> className_servletName;

    private WebappClassLoader webappClassLoader;

    private Host host;

    private boolean reloadable;

    private ContextFileChangeWatcher contextFileChangeWatcher;

    private ServletContext servletContext;

    private Map<Class<?>, HttpServlet> servletPool;

    private Map<String, Map<String, String>> servletClassNameInitParams;

    private List<String> loadOnStartupServletClassNames;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.url_servletClassName = new HashMap<>();
        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        this.host = host;
        this.reloadable = reloadable;
        this.servletPool = new HashMap<>();
        this.servletClassNameInitParams = new HashMap<>();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        this.webappClassLoader = new WebappClassLoader(docBase, contextClassLoader);

        this.servletContext = new ApplicationContext(this);
        this.loadOnStartupServletClassNames = new ArrayList<>();
        deploy();

    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    private void parseServletMapping(Document d) {
        Elements elements = d.select("servlet-mapping url-pattern");
        for (Element element : elements) {
            String urlPattern = element.text();
            String servletName = element.parent().select("servlet-name").first().text();
            url_servletName.put(urlPattern, servletName);
        }
        Elements sevletNameElements = d.select("servlet servlet-name");
        for (Element sevletNameElement : sevletNameElements) {
            String sevletName = sevletNameElement.text();
            String servletClass = sevletNameElement.parent().select("servlet-class").first().text();
            servletName_className.put(sevletName, servletClass);
            className_servletName.put(servletClass, sevletName);
        }
        Set<String> urls = url_servletName.keySet();
        for (String url : urls) {
            String servletName = url_servletName.get(url);
            String servletClassName = servletName_className.get(servletName);
            url_servletClassName.put(url, servletClassName);
        }
    }


    private void checkDuplicate(Document d, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = d.select(mapping);
        List<String> contexts = new ArrayList<>();
        for (Element element : elements) {
            contexts.add(element.text());
        }
        Collections.sort(contexts);
        for (int i = 0; i < contexts.size() - 1; i++) {
            String content = contexts.get(i);
            String contentNext = contexts.get(i);
            if (content.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, content));
            }

        }
    }

    private void checkDucplicate() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        checkDuplicate(d, "servlet-mapping url-pattern", "servlet url重复，请保持其唯一性:{}");
        checkDuplicate(d, "servlet servlet-name", "servlet 名称重复，请保持其唯一性:{}");
        checkDuplicate(d, "servlet servlet-class", "servlet 类名重复，请保持其唯一性:{}");
    }

    private void init() {
        //判断是否有web.xml文件
        if (!contextWebXmlFile.exists()) {
            return;
        }
        try {
            checkDucplicate();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }

        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document document = Jsoup.parse(xml);
        parseServletMapping(document);
        parseServletInitParams(document);
        parseLoadOnStartup(document);
        handleLoadOnStartup();
    }

    private void deploy() {
        init();
        if (isReloadable()) {
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }
    }

    public String getServletClassName(String uri) {
        return url_servletClassName.get(uri);
    }

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    public void reload() {
        host.reload(this);
    }

    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public synchronized HttpServlet getServlet(Class<?> clazz) throws IllegalAccessException, InstantiationException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();

            ServletContext servletContext = this.getServletContext();
            String className = clazz.getName();
            String servletName = className_servletName.get(className);

            Map<String, String> initParams = servletClassNameInitParams.get(className);
            StandardServletConfig servletConfig = new StandardServletConfig(servletContext, servletName, initParams);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    private void parseServletInitParams(Document d) {
        Elements servletClassNameElements = d.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            Map<String, String> initParams = new HashMap<>();
            for (Element initElement : initElements) {
                String name = initElement.select("param-name").get(0).text();
                String value = initElement.select("param-value").get(0).text();
                initParams.put(name, value);
            }
            servletClassNameInitParams.put(servletClassName, initParams);
        }

    }

    /** 自启动类
     * @param d
     */
    private void parseLoadOnStartup(Document d) {
        Elements es = d.select("load-on-startup");
        for (Element e : es) {
            String loadOnStartupServletClassName = e.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }


    /**
     * 自启动类被webappClassLoader加载
     */
    public void handleLoadOnStartup() {
        for (String loadOnStartupServletClassName : loadOnStartupServletClassNames) {
            try {
                Class<?> clazz = webappClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            } catch (ClassNotFoundException|IllegalAccessException|ServletException|InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
