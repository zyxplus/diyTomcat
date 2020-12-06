package com.cs.tomcat.http;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpResponse;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.util.Constant;

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
     * 根据请求的uri获取servletClassName
     * 根据上下文选择类加载器，加载并实例化
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

        try {
            Class servletClass = context.getWebappClassLoader().loadClass(servletClassName);
            System.out.println("servletClass: " + servletClass);
            System.out.println("servletClass'classLoader:" + servletClass.getClassLoader());
            Object servletObject = ReflectUtil.newInstance(servletClassName);
            ReflectUtil.invoke(servletObject, "service", request, response);
            response.setStatus(Constant.CODE_200);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
