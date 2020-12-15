package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.*;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.SessionManager;
import com.cs.tomcat.util.WebXMLUtil;
import com.cs.tomcat.webappservlet.HelloServlet;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpProcessor {
    public void execute(Socket s, Request request, Response response) {
        try {
            Context context = request.getContext();
            String uri = request.getUri();
            prepareSession(request, response);

            //web.XML中存在的话，需要通过反射创建对象servlet容器
            String servletClassName = context.getServletClassName(uri);

            if (null != servletClassName) {
                InvokerServlet.getInstance().service(request, response);
            } else if (uri.endsWith(".jsp")) {
                JspServlet.getInstance().service(request,response);
            }
            else {
                //进行Web访问时首先所有的请求都会进入Tomcat，然后这些请求都会先流经DefaultServlet，
                // 接着再流到指定的Servlet上去，如果没有匹配到任何应用指定的servlet，那么就会停留在
                // DefaultServlet
                DefaultServlet.getInstance().service(request, response);
            }

            if (Constant.CODE_200 == response.getStatus()){
//                handle200(s, response);
                handle200(s, request, response);
                return;
            }
            if (Constant.CODE_404 == response.getStatus()){
                handle404(s, uri);
                return;
            }

        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(s, e);
        } finally {
            try {
                if (!s.isClosed()) {
                    s.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareSession(Request request, Response response) {
        String jsessionid = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jsessionid, request, response);
        request.setSession(session);
    }


    /**
     * 成功连接，把响应输出
     *
     * @param s
     * @param response
     * @throws IOException
     */
    private static void handle200(Socket s, Response response) {
        try {
            String contentType = response.getContentType();
            String headText = Constant.RESPONSE_HEAD_202;

            String cookiesHeader = response.getCookiesHeader();
            headText = StrUtil.format(headText, contentType, cookiesHeader);


            //把字节数据转化成字节数组
            byte[] head = headText.getBytes();
            byte[] body = response.getBody();
            byte[] responseBytes = new byte[head.length + body.length];

            ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
            ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
            //打开输出流向客户端输出
            OutputStream outputStream = null;
            outputStream = s.getOutputStream();
            outputStream.write(responseBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handle200(Socket s, Request request, Response response) throws IOException {
        OutputStream os = s.getOutputStream();
        String contentType = response.getContentType();

        byte[] body = response.getBody();
        String cookiesHeader = response.getCookiesHeader();

        boolean gzip = isGzip(request, body, contentType);

        String headText;
        if (gzip) {
            headText = Constant.RESPONSE_HEAD_200_GZIP;
        } else {
            headText = Constant.RESPONSE_HEAD_200;
        }
        headText = StrUtil.format(headText, contentType, cookiesHeader);
        if (gzip) {
            body = ZipUtil.gzip(body);
        }
        byte[] head = headText.getBytes();
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
        os.write(responseBytes, 0, responseBytes.length);
        os.flush();
        os.close();

    }

    private static boolean isGzip(Request request, byte[] body, String mimeType) {
        String acceptEncodings = request.getHeader("Accept-Encoding");
        if (!StrUtil.containsAny(acceptEncodings, "gzip")){
            return false;
        }
        Connector connector = request.getConnector();
        if (mimeType.contains(";")) {
            mimeType = StrUtil.subBefore(mimeType, ";", false);
        }
        if (!"on".equals(connector.getCompression())) {
            return false;
        }
        if (body.length < connector.getCompressionMinSize()) {
            return false;
        }

        //对某些浏览器内核（gozilla, traviata）不做压缩
        String userAgents = connector.getNoCompressionUserAgents();
        String[] eachUserAgents = userAgents.split(",");
        for (String eachUserAgent : eachUserAgents) {
            eachUserAgent = eachUserAgent.trim();
            String userAgent = request.getHeader("User-Agent");
            if (StrUtil.containsAny(userAgent, eachUserAgent)){
                return false;
            }
        }

        //限制可以压缩的MIME类型
        String mimeTypes = connector.getCompressableMimeType();
        String[] eachMineTypes = mimeTypes.split(",");
        for (String eachMineType : eachMineTypes) {
            if (mimeType.equals(eachMineType)) {
                return true;
            }
        }
        return false;

    }


    private static void handle404(Socket s, String uri) throws IOException {
        OutputStream outputStream = s.getOutputStream();
        String responseText = StrUtil.format(Constant.TEXT_FORMAT_404, uri, uri);
        responseText = Constant.RESPONSE_HEAD_404 + responseText;
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);
    }

    private static void handle500(Socket s, Exception e) {
        try {
            OutputStream outputStream = s.getOutputStream();
            StackTraceElement[] stackTraces = e.getStackTrace();
            StringBuffer stringBuffer = new StringBuffer();
            //Exception名字
            stringBuffer.append(e.toString());
            stringBuffer.append("\r\n");

            //Exception详情
            for (StackTraceElement stackTrace : stackTraces) {
                stringBuffer.append("\t");
                stringBuffer.append(stackTrace.toString());
                stringBuffer.append("\r\n");
            }
            String msg = e.getMessage();

            if (null != msg && msg.length() > 0) {
                msg = msg.substring(0, 19);
            }
            //填入500response
            String text = StrUtil.format(Constant.TEXT_FORMAT_500, msg, e.toString(), stringBuffer.toString());
            text = Constant.RESPONSE_HEAD_500 + text;
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


}
