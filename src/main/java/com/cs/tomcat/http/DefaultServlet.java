package com.cs.tomcat.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.WebXMLUtil;
import com.cs.tomcat.webappservlet.HelloServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class DefaultServlet extends HttpServlet {

    private static DefaultServlet instance = new DefaultServlet();

    private DefaultServlet() {};

    public static synchronized DefaultServlet getInstance() {
        return instance;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;

        Context context = request.getContext();
        String uri = request.getUri();

        if ("/500.html".equals(uri)) {
            throw new RuntimeException("500 exception");
        }

        if ("/".equals(uri)) {
            uri = WebXMLUtil.getWelcomeFile(request.getContext());
        }

        if (uri.endsWith(".jsp")) {
            JspServlet.getInstance().service(request, response);
            return;
        }

        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(request.getRealPath(fileName));

        if (file.exists()) {
            String extName = FileUtil.extName(file);
            String mimeType = WebXMLUtil.getMimeType(extName);
            response.setContentType(mimeType);

            //文件读取成二进制，放入response的body
            byte[] body = FileUtil.readBytes(file);
            response.setBody(body);

            if (fileName.equals("timeConsume.html")) {
                ThreadUtil.sleep(1000);
            }
            response.setStatus(Constant.CODE_200);
        } else {
            response.setStatus(Constant.CODE_404);
        }

    }
}
