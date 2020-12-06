package com.cs.tomcat.http;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpResponse;
import com.cs.tomcat.catalina.Context;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InvokerServlet extends HttpServlet {

    private static InvokerServlet instance = new InvokerServlet();

    private InvokerServlet() {}

    public static synchronized InvokerServlet getInstance() {
        return instance;
    }


    /**
     * 根据请求的uri获取servletClassName并实例化
     * @param httpServletRequest
     * @param httpServletResponse
     */
    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        Object servletObject = ReflectUtil.newInstance(servletClassName);
        ReflectUtil.invoke(servletObject, "service", request, response);
    }

}
