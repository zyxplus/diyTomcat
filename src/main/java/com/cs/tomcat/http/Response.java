package com.cs.tomcat.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Response extends BaseResponse{
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String contentType;
    private byte[] body;
    private int status;

    public Response() {
        this.stringWriter = new StringWriter();
        this.contentType = "text/html";
        this.printWriter = new PrintWriter(stringWriter);
    }


    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    //只初始化一次
    public byte[] getBody() {
        if (null == body) {
            String content = stringWriter.toString();
            byte[] body = content.getBytes(StandardCharsets.UTF_8);
        }
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }
}
