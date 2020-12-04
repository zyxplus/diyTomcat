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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Server {
    private Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        logJVM();
        init();
    }


    private static void logJVM() {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "How2j DiyTomcat/1.0.1");
        infos.put("Server built", "2020-04-08 10:20:22");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("java.home"));
        infos.put("Java Home", SystemUtil.get("java home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));
        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + ":\t\t" + infos.get(key));
        }
    }

    private void init() {
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
                            System.out.println(uri);
                            if ("/".equals(uri)) {
                                String html = "Hello DIY Tomcat ";
                                response.getPrintWriter().println(html);
                            } else {
//                                fileHandler(uri, response);
                                fileHandlerJUC(uri, response, context, s);
                            }
                            handle200(s, response);

                        } catch (IOException e) {
                            e.printStackTrace();
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
     * @param s
     * @param response
     * @throws IOException
     */
    private static void handle200(Socket s, Response response) throws IOException {
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
        OutputStream outputStream = s.getOutputStream();
        outputStream.write(responseBytes);
    }

    /**
     * 从Constant.rootFolder目录下面读取文件并返回
     * @param uri
     * @param response
     */
    private static void fileHandler(String uri, Response response) {
        //处理文件
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(Constant.ROOT_FOLDER, fileName);
        if (file.exists()) {
            String fileContent = FileUtil.readUtf8String(file);
            response.getPrintWriter().println(fileContent);
        } else {
            response.getPrintWriter().println("File not found");
        }
    }

    /**
     * 多线程文件读取测试
     * 读到timeConsume.html 则挂起一秒
     * @param uri
     * @param response
     */
    private static void fileHandlerJUC(String uri, Response response, Context context, Socket s) {
        //处理文件
        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(context.getDocBase(), fileName);
        if (file.exists()) {
            String fileContent = FileUtil.readUtf8String(file);
            response.getPrintWriter().println(fileContent);
            if (fileName.equals("timeConsume.html")) {
                ThreadUtil.sleep(1000);
            }
        } else {
//            response.getPrintWriter().println("File not found");
            handle404(s, uri);
        }
    }

    private static void handle404(Socket s, String uri) {

    }


}
