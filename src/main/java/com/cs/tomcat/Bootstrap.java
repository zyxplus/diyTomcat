package com.cs.tomcat;

import cn.hutool.core.util.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Bootstrap {

    public static void main(String[] args) {
        try {
            int port = 18080;

            //判断端口占用
            if (!NetUtil.isUsableLocalPort(port)) {
                System.out.println(port + "端口已经被占用");
                return;
            }

            //新建socket通信
            ServerSocket ss = new ServerSocket();

            while (true) {
                //接收浏览器客户端的请求
                Socket s = ss.accept();
                //接收浏览器的提交信息
                InputStream is = s.getInputStream();
                //读取浏览器信息
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                is.read(buffer);
                //把字节数据转化成层字符串并打印
                String requestString = new String(buffer, "UTF-8");
                System.out.println("浏览器输入信息: \r\n" + requestString);
                //打开输出流向客户端输出
                OutputStream outputStream = s.getOutputStream();
                //准备发送的数据
                String responseHead = "HTTP/1.1 200 OK \r\n" + "Content-Type:text/html \r\n\r\n";
                String responseString = "<div style='color:blue' >Hello DIY Tomcat!</div>";
                requestString = responseHead + requestString;
                //字符串转换成字节数组发出
                outputStream.write(requestString.getBytes());
                outputStream.flush();
                //关闭socket
                s.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
