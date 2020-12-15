package com.cs.tomcat.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.cs.tomcat.Bootstrap;
import com.cs.tomcat.catalina.Context;
import com.cs.tomcat.catalina.Engine;
import com.cs.tomcat.catalina.Service;
import com.cs.tomcat.util.MiniBrowser;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

public class Request extends BaseRequest {

    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;
    private Service service;
    private String method;
    private String queryString;
    private Map<String, String[]> parameterMap;
    private Map<String, String> headerMap;

    private Cookie[] cookies;
    private HttpSession session;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        this.parameterMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) {
            return;
        }
        parseUri();
        parseContext();
        parseMethod();
        //修正路径
        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            //不存在则返回根目录
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }
        parseParameters();
        parseHeaders();
        parseCookies();

    }

    public String getJSessionIdFromCookie(){
        if (null == cookies) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void parseCookies() {
        List<Cookie> cookieList = new ArrayList<>();
        String cookieStr = headerMap.get("cookie");
        if (null != cookieStr) {
            String[] pairs = StrUtil.split(cookieStr, ";");
            for (String pair : pairs) {
                if (StrUtil.isBlank(pair)) {
                    continue;
                }
                String[] segs = StrUtil.split(pair, "=");
                String name = segs[0].trim();
                String value = segs[1].trim();
                Cookie cookie = new Cookie(name, value);
                cookieList.add(cookie);
            }
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);

    }

    /**
     * 请求体转成字符串
     * @throws IOException
     */
    private void parseHttpRequest() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream, false);
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

    public Context getContext() {
        return  context;
    }

    /**
     * 解析uri请求中的context
     */
    private void parseContext() {
        Engine engine = service.getEngine();
        Context context = engine.getDefaultHost().getContext(uri);
        if (null != context) {
            return;
        }

        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path) {
            // 如果uri = /timeConsume.html，那么path = null， 经过此处之后path=/
            path = "/";
        } else {
            // uri = /dir1/1.html, 那么path= dir1， 经过此处之后path=/dir1
            path = "/" + path;
        }
        this.context = service.getEngine().getDefaultHost().getContext(path);
        if (null == this.context) {
            // 如果没有获取到这个context对象，那么说明目录中根本就没有这个应用,或者本身就在根目录下
            this.context = service.getEngine().getDefaultHost().getContext("/");
        }
    }

    private void parseMethod() {
        method = StrUtil.subBefore(requestString, " ", false);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getRealPath(String path) {
        return context.getServletContext().getRealPath(path);
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    /**
     * 把URL里面的参数存到 parameterMap
     */
    private void parseParameters() {
        if ("GET".equals(this.getMethod())) {
            String url = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(url, '?')) {
                queryString = StrUtil.subAfter(url, '?', false);
            }
        }
        if ("POST".equals(this.getMethod())) {
            StrUtil.subAfter(requestString, "\r\n\r\n", false);
        }
        if (null == queryString || 0 == queryString.length()) {
            return;
        }

        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");
        if (null != parameterValues) {
            for (String parameterValue : parameterValues) {
                String[] nameValues = parameterValue.split("=");
                String name = nameValues[0];
                String value = nameValues[1];
                String[] values = parameterMap.get(name);
                if (null == values) {
                    values = new String[]{value};
                    parameterMap.put(name, values);
                } else {
                    values = ArrayUtil.append(values, value);
                    parameterMap.put(name, values);
                }
            }
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (null != values && 0 != values.length) {
            return values[0];
        }
        return null;
    }

    @Override
    public Map getParameterMap() {
        return parameterMap;
    }

    public Enumeration getParameternames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public String getHeader(String name) {
        if (null == name) {
            return null;
        }
        name = name.toLowerCase();
        return headerMap.get(name);
    }

    @Override
    public int getIntHeader(String name) {
        String value = headerMap.get(name);
        return Convert.toInt(value, 0);
    }

    public void parseHeaders(){
        StringReader stringReader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(stringReader, lines);
        for (String line : lines) {
            if (line.length() == 0) {
                break;
            }
            String[] segs = line.split(":");
            String headerName = segs[0].toLowerCase();
            String headerValue = segs[1];
            headerMap.put(headerName, headerValue);
        }
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getProtocol() {
        return "HTTP:/1.1";
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        String temp = isa.getAddress().toString();
        return StrUtil.subAfter(temp, "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        return isa.getHostName();
    }

    @Override
    public int getRemotePort() {
        return socket.getPort();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getServerPort() {
        return getLocalPort();
    }

    @Override
    public String getContextPath() {
        String result = this.context.getPath();
        if ("/".equals(result)) {
            return "";
        }
        return result;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80;
        }
        url.append(scheme);
        url.append("//");
        url.append(getServerName());
        if (((scheme.equals("http")) && (port != 80)) || (scheme.equals("https")) && (port != 80)) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() {
        return uri;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
}

