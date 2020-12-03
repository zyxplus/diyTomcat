package com.cs.tomcat.util;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.cs.tomcat.util.MiniBrowser.getHttpString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 浏览器
 */
public class MiniBrowser {

    public static void main(String[] args) {
        String url = "http://127.0.0.1:18082";
        String contentString = getContentString(url, false);
        System.out.println(contentString);
        System.out.println("===============");
        String httpString = getHttpString(url, false);
        System.out.println(httpString);
    }

    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false);
    }

    public static String getContentString(String url) {
        return getContentString(url, false);
    }


    /**
     * 从url获取字符数组，转换成utf8编码的字符串
     * @param url
     * @param gzip
     * @return
     */
    public static String getContentString(String url, boolean gzip) {
        byte[] result = getContentBytes(url, gzip);
        if (null == result) {
            return null;
        }
        //引入标准的UTF-8后就不用写catch了
        return new String(result, UTF_8).trim();
    }

    /**
     * 提取响应里的response body, 返回字符数组
     * @param url
     * @param gzip
     * @return
     */
    public static byte[] getContentBytes(String url, boolean gzip) {
        byte[] response = getHttpBytes(url, gzip);
        byte[] doubleReturn = "\r\n\r\n".getBytes();
        int pos = -1;
        for (int i = 0; i < response.length - doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);
            if (Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        //判断响应是否为空, pos为响应头长度
        if (-1 == pos) {
            return null;
        }
        pos += doubleReturn.length;
        //返回响应体
        byte[] result = Arrays.copyOfRange(response, pos, response.length);
        return result;
    }

    public static String getHttpString(String url, boolean gzip) {
        byte[] bytes = getHttpBytes(url, gzip);
        return new String(bytes).trim();
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false);
    }

    /**
     * 建立socket
     * 写好request
     * 发出 : outputStream -> OutputStreamWriter
     * 接收response: inputStream -> buffer -> byteArray
     *
     * @param url
     * @param gzip
     * @return
     */
    public static byte[] getHttpBytes(String url, boolean gzip) {
        byte[] result = null;
        try {
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if (-1 == port) {
                port = 80;
            }

            //通过URL找到目的主机+端口
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            //尝试连接，超时断开
            client.connect(inetSocketAddress, 1000);
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Host", u.getHost() + ":" + port);
            //告知（服务器）客户端可以处理的内容类型
            requestHeaders.put("Accept", "test/html");
            //User-Agent会告诉网站服务器，访问者是通过什么工具来请求的，如果是爬虫请求，一般会拒绝，如果是用户浏览器，就会应答
            requestHeaders.put("User-Agent", "how2j mini brower / java1.8");

            if (gzip) {
                requestHeaders.put("Accept-Encoding", "gzip");
            }

            String path = u.getPath();
            if (path.length() == 0) {
                path = "/";
            }

            String firstLine = "Get " + path + " HTTP/1.1\r\n";

            //用StringBuffer() 拼装请求
            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headerLine);
            }

            //client发出请求信息
            PrintWriter printWriter = new PrintWriter(client.getOutputStream(), true);
            printWriter.println(httpRequestString);

            //client接收响应
            InputStream inputStream = client.getInputStream();
            result = readBytes(inputStream);
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
            //???
            result = e.toString().getBytes(UTF_8);
        }
        return result;
    }


    /**
     * @param inputStream socket的输入流
     * @return 字节数组
     * @throws IOException
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        int buffer_size = 1024;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[buffer_size];
        while (true) {
            int length = inputStream.read(buffer);
            if (-1 == length) {
                break;
            }
            baos.write(buffer, 0, length);
            if (length != buffer_size) {
                break;
            }
        }
        //拷贝buffer里的响应
        return baos.toByteArray();
    }
}
