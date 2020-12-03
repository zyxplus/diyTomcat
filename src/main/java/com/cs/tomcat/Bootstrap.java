package com.cs.tomcat;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Bootstrap {

    public static void main(String[] args) {
        try {
            int port = 18081;

            //判断端口占用
            if (!NetUtil.isUsableLocalPort(port)) {
                System.out.println(port + "端口已经被占用");
                return;
            }

            //新建socket通信
            ServerSocket ss = new ServerSocket(port);

            while (true) {

                //接收浏览器客户端的请求
                Socket s = ss.accept();
                //接收浏览器的提交信息
                Request request = new Request(s);

                System.out.println("浏览器输入信息: \r\n" + request.getRequestString());
                System.out.println("uri:" + request.getUri());

                Response response = new Response();
                String html = "Hello DIY Tomcat ";
                response.getPrintWriter().println(html);

                handle200(s, response);
            }

        } catch (IOException e) {
            e.printStackTrace();
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
        String headText = Constant.RESPONSER_HEAD_202;
        headText = StrUtil.format(headText, contentType);
        //把字节数据转化成字符数组
        byte[] head = headText.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[head.length + body.length];

        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
        //打开输出流向客户端输出
        OutputStream outputStream = s.getOutputStream();
        outputStream.write(responseBytes);
        s.close();

    }


}
