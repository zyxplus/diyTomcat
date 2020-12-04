package com.cs.tomcat.catalina;

import cn.hutool.log.LogFactory;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Host {
    private String name;
    private Map<String, Context> contextMap;
    private Engine engine;

    public Host() {
        this.contextMap = new HashMap<>();
        this.name = ServerXMLUtil.getHostName();
    }

    public Host(String name, Engine engine) {
        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Context> getContextMap() {
        return contextMap;
    }

    public void setContextMap(Map<String, Context> contextMap) {
        this.contextMap = contextMap;
    }

    /**
     * 穷举文件资源，得到contextMap<名字,（名字，绝对路径）>
     */
    private void scanContextOnWebAppsFolder() {
        File[] folders = Constant.WEBAPPS_FOLDER.listFiles();
        if (folders == null) {
            LogFactory.get().error(new NoSuchFieldError());
            return;
        }
        for (File folder : folders) {
            if (!folder.isDirectory()) {
                continue;
            }
            loadContext(folder);
        }
    }

    private void loadContext(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);
        contextMap.put(context.getPath(), context);
    }

    private void scanContextsServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

}
