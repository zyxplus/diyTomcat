package com.cs.tomcat.catalina;

import cn.hutool.core.util.NetUtil;
import cn.hutool.log.LogFactory;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.ThreadPoolUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
                        Response response = new Response();
                        Request request = null;
                        try {
                            request = new Request(s, service);
                            HttpProcessor httpProcessor = new HttpProcessor();
                            httpProcessor.execute(s, request, response);
                        } catch (IOException e) {
                            LogFactory.get().error(e);
                        }
                    }
                };
                ThreadPoolUtil.run(runnable);
            }
        } catch (IOException e) {
            LogFactory.get().error(e);
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
