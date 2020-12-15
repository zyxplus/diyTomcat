package com.cs.tomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.cs.tomcat.http.Request;
import com.cs.tomcat.http.Response;
import com.cs.tomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class SessionManager {

    //存session
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    //默认失效时间
    private static int defaultTimeout = getTimeout();

    static {
        startSessionOutdateCheckThread();
    }


    /**
     * @return 获取web.xml里的失效时间
     */
    private static int getTimeout() {
        int defaultResult = 30;
        try {
            Document d = Jsoup.parse(Constant.WEB_XML_FILE, "utf-8");
            Elements es = d.select("session-config session-timeout");
            if (es.isEmpty()) {
                return defaultResult;
            }
            return Convert.toInt(es.get(0).text());
        } catch (IOException e) {
            return defaultResult;
        }
    }


    /**
     * 根据jsessionid判断是否要创建新的会话
     * @param jsessionid 浏览器可能传来的jsessionid
     * @param request
     * @param response
     * @return
     */
    public static HttpSession getSession(String jsessionid, Request request, Response response) {
        if (null == jsessionid) {
            return newSession(request, response);
        } else {
            StandardSession currentSession = sessionMap.get(jsessionid);
            if (null == currentSession) {
                return newSession(request, response);
            } else {
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, request, response);
                return currentSession;
            }
        }
    }

    //会话超时设置 + 把request里的请求路径放进response
    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }


    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, request, response);
        return session;
    }


    /**
     * 从sessionMap里面根据lastAccessTime筛选出过期的jsessionids，然后把他们从sessionMap里去掉
     */
    private static void checkOutDateSession() {
        Set<String> jsessionIds = sessionMap.keySet();
        List<String> outdateJessionIds = new ArrayList<>();

        for (String jsessionId : jsessionIds) {
            StandardSession session = sessionMap.get(jsessionId);
            long interval = System.currentTimeMillis() - session.getLastAccessedTime();
            if (interval > session.getMaxInactiveInterval() * 1000) {
                outdateJessionIds.add(jsessionId);
            }
        }

        for (String jessionId : outdateJessionIds) {
            sessionMap.remove(jessionId);
        }
    }


    /**
     * 定时调用checkOutDateSession
     */
    private static void startSessionOutdateCheckThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    checkOutDateSession();
                    ThreadUtil.sleep(1000 * 3);
                }
            }
        }.start();
    }

    /**
     * @return 创建sessionId (随机字符串 + md5加密 + 纯大写)
     */
    public static synchronized String generateSessionId() {
        String result = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();
        return result;
    }




}
