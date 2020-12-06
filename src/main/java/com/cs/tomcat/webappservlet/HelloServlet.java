package com.cs.tomcat.webappservlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
    public HelloServlet() {
        System.out.println(this + " construct() ");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            System.out.println(this + " doGet()");
            resp.getWriter().println("Hello DIY Tomcat from HelloServlet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) {
        String author = config.getInitParameter("author");
        String site = config.getInitParameter("site");
        System.out.println("init: author = ");
        System.out.println("init: site = ");
    }

    @Override
    public void destroy() {
        System.out.println(this + " destroy()");
    }
}
