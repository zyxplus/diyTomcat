package com.cs.tomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;
import com.cs.tomcat.util.ThreadPoolUtil;
import com.cs.tomcat.util.WebXMLUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Connector implements Runnable {

    int port;
    private Service service;

    public Connector(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            int port = 18081;

            //判断端口占用
            if (!NetUtil.isUsableLocalPort(port)) {
                System.out.println(port + "端口已经被占用");
                return;
            }

            //新建socket通信，绑定端口
            ServerSocket ss = new ServerSocket(port);

            while (true) {

                //开始监听端口
                final Socket s = ss.accept();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(s, service);
                            Context context = request.getContext();
                            Response response = new Response();
                            String uri = request.getUri();
                            if ("/".equals(uri)) {
                                //跳至欢迎页
                                uri = WebXMLUtil.getWelcomeFile(request.getContext());
                            } else {
                                fileHandlerJUC(uri, response, context, s);
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
                };
                ThreadPoolUtil.run(runnable);

            }

        } catch (IOException e) {
            e.printStackTrace();
            LogFactory.get().error(e);
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

    public void init() {
        LogFactory.get().info("Initializing protocolHanddler [http-bio-{}]", port);
    }

    public void start() {
        LogFactory.get().info("Starting protocolHanddler [http-bio-{}]", port);
        new Thread(this).start();
    }

}