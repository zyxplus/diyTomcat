package com.cs.test.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.util.ThreadPoolUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cs.tomcat.util.MiniBrowser;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestTomcat {
    //在IDEA debugger里面改
    private static int port = 18081;
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

    /**
     * 要访问什么资源
     * @param uri
     * @return
     */
    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}",ip,port,uri);
        return MiniBrowser.getContentString(url);
    }


    @Test
    public void testHtml() {
        String html = getContentString("/a.html");
        Assert.assertEquals(html,"Hello DIY Tomcat from a.html");

    }

    @Test
    public void testTImeConsumeHtml() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>());
        //开始计时
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < 3; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    getContentString("/timeConsume.html");
                }
            });
        }
        threadPoolExecutor.shutdown();
        //关停命令下达后给予最后1小时运行时间
        threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
        //结束计时
        long duration = timeInterval.intervalMs();
        Assert.assertTrue(duration < 3000);
    }


}
