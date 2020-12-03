package com.cs.tomcat.http;

import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.Bootstrap;
import com.cs.tomcat.catalina.Context;
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

    public Request(Socket socket) throws IOException {
        this.socket = socket;
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
            return;
        }
        parseUri();
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
     *         获取两个空格之间的内容,
     *         如果地址是 http://127.0.0.1:18080/index.html?name=gareen
     *         那么http请求就会是
     *         GET /index.html?name=gareen HTTP/1.1
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
     * Bootstrap.contextMap 是静态方法，包含所有路径下的context信息
     * 从uri获取path: "cd/addg/asfg/asfg" 返回 "addg"
     * 把uri请求的资源与request绑定
     */
    private void parseContext() {
        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path) {
            path = "/";
        } else {
            path = "/" + path;
        }
        context = Bootstrap.contextMap.get(path);
        if (null == context) {
            context = Bootstrap.contextMap.get("/");
        }
    }

}
