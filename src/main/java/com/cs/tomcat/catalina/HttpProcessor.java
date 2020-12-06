package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.InvokerServlet;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.WebXMLUtil;
import com.cs.tomcat.webappservlet.HelloServlet;

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
            if (null == uri) {
                return;
            }

            //web.XML中存在的话，需要通过反射创建对象servlet容器
            String servletClassName = context.getServletClassName(uri);
            if (null != servletClassName) {
                InvokerServlet.getInstance().service(request, response);
            } else {
                //HelloServlet处理
                if ("/hello".equals(uri)) {
                    HelloServlet helloServlet = new HelloServlet();
                    helloServlet.doGet(request, response);
                } else {
                    //跳至欢迎页
                    if ("/".equals(uri)) {
                        uri = WebXMLUtil.getWelcomeFile(request.getContext());
                    } else {
                        fileHandlerJUC(uri, response, context, s);
                    }
                }
            }

            handle200(s, response);
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
            headText = StrUtil.format(headText, contentType);
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

    /**
     * 从Constant.rootFolder目录下面读取文件并返回
     *
     * @param uri
     * @param response
     */
    private static void fileHandler(String uri, Response response) {
        //处理文件
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(Constant.ROOT_FOLDER, fileName);
        if (file.exists()) {
            //获取拓展名
            String extName = FileUtil.extName(file);

            String fileContent = FileUtil.readUtf8String(file);
            response.getPrintWriter().println(fileContent);
        } else {
            response.getPrintWriter().println("File not found");
        }
    }

    /**
     * 多线程文件读取测试
     * 读到timeConsume.html 则挂起一秒
     *
     * @param uri
     * @param response
     */
    private static void fileHandlerJUC(String uri, Response response, Context context, Socket s) throws IOException {
        //处理文件
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(context.getDocBase(), fileName);
        if (file.exists()) {
            //文件读取成二进制，放入response的body
            byte[] body = FileUtil.readBytes(file);
            response.setBody(body);
            if (fileName.equals("timeConsume.html")) {
                ThreadUtil.sleep(1000);
            }
        } else {
//            response.getPrintWriter().println("File not found");
            handle404(s, uri);
        }
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
