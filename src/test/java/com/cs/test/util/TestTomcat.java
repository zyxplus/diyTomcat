package com.cs.test.util;

import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cs.tomcat.util.MiniBrowser;

public class TestTomcat {
    //在IDEA debugger里面改
    private static int port = 18080;
    private static String ip = "127.0.0.1";

    @BeforeClass
    public static void beforeClass() {
        if (NetUtil.isUsableLocalPort(port)) {
            System.out.println(port + " 端口不可用");
            System.exit(1);
        } else {
            System.out.println("开始单元测试");
        }
    }

    @Test
    public void testHelloTomcat() {
        String html = getContentString("/");
        Assert.assertEquals(html, "<div style='color:blue' >Hello DIY Tomcat!</div>");
    }

    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
        return MiniBrowser.getContentString(url);
    }


}
