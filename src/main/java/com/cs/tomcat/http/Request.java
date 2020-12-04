package com.cs.tomcat.http;

import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.Bootstrap;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.catalina.Engine;
import com.cs.tomcat.catalina.Service;
import com.cs.tomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Request {

    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;
    private Service service;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
            return;
        }
        parseUri();
        parseContext();
        //修正路径
        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
        }
    }

    /**
     * 请求体转成字符串
     * @throws IOException
     */
    private void parseHttpRequest() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream);
        requestString = new String(bytes, StandardCharsets.UTF_8);
    }


    /**
     *  解析uri，定位服务器上的文件：
     *         获取两个空格之间的内容,
     *         如果地址是 http://127.0.0.1:18080/index.html?name=gareen
     *         那么http请求就会是
     *         GET /index.html?name=green HTTP/1.1
     *         Host: 127.0.0.1:18080
     *         Connection: keep-alive
     *         。。。。
     *         只需要获取两个空格之间的部分就可以获得请求的uri
     */
    private void parseUri() {
        String temp;
        temp = StrUtil.subBetween(requestString, " ", " ");
        if (!StrUtil.contains(temp, '?')) {
            uri = temp;
            return;
        }
        uri = StrUtil.subBefore(temp, "?", false);
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

    public Context getContext() {
        return  context;
    }

    /**
     * 解析uri请求中的context
     */
    private void parseContext() {
        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path) {
            // 如果uri = /timeConsume.html，那么path = null， 经过此处之后path=/
            path = "/";
        } else {
            // uri = /dir1/1.html, 那么path= dir1， 经过此处之后path=/dir1
            path = "/" + path;
        }
        context = service.getEngine().getDefaultHost().getContext(path);
        if (null == context) {
            // 如果没有获取到这个context对象，那么说明目录中根本就没有这个应用,或者本身就在根目录下
            context = service.getEngine().getDefaultHost().getContext("/");
        }
    }

}
