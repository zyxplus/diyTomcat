package com.cs.tomcat.http;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.JspUtil;
import com.cs.tomcat.util.WebXMLUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class JspServlet extends HttpServlet {
    private static JspServlet instance = new JspServlet();
    public static synchronized JspServlet getInstance() {
        return instance;
    }

    private JspServlet(){}

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();

            if ("/".equals(uri)) {
                uri = WebXMLUtil.getWelcomeFile(request.getContext());
            }

            String filename = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(request.getRealPath(filename));
            File jspFile = file;
            if (jspFile.exists()) {

                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;

                if ("/".equals(path)) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, "/", false);
                }

                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);

                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, jspFile);
                } else if (file.lastModified() > jspServletClassFile.lastModified()) {
                    JspUtil.compileJsp(context, jspFile);
                }

                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                byte[] body = FileUtil.readBytes(file);
                response.setBody(body);
                response.setStatus(Constant.CODE_200);
            } else {
                response.setStatus(Constant.CODE_404);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
