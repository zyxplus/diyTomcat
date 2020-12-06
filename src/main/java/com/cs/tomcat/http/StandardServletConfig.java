package com.cs.tomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;
    private Map<String, String> initPatameters;
    private String servletName;

    public StandardServletConfig(ServletContext servletContext,
                                 String servletName,
                                 Map<String, String> initPatameters) {
        this.servletContext = servletContext;
        this.initPatameters = initPatameters;
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initPatameters.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initPatameters.keySet());
    }
}
