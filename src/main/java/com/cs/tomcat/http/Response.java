package com.cs.tomcat.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Response {
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String contentType;

    public Response() {
        this.stringWriter = new StringWriter();
        this.contentType = "text/html";
        this.printWriter = new PrintWriter(stringWriter);
    }

    public String getContentType() {
        return contentType;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public byte[] getBody() {
        String content = stringWriter.toString();
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        return body;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
