package com.cs.tomcat.http;

import com.cs.tomcat.catalina.Context;

import java.io.File;
import java.util.*;

public class ApplicationContext extends BaseServletContext {

    private Map<String , Object> attributesMap;
    private Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    public void setAttributesMap(String name, Object value) {
        attributesMap.put(name, value);
    }

    public Map<String, Object> getAttributeMap(String name) {
        return attributesMap;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }

}
