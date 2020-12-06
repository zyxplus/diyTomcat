package com.cs.tomcat;

import com.cs.tomcat.catalina.Server;
import com.cs.tomcat.catalina.classloader.CommonClassLoader;

import java.lang.reflect.Method;

public class Bootstrap {

    /** 如果基础类调用用户代码: 引入线程上下文类加载器
     *  catalinaLoader：Tomcat容器私有的类加载器，加载路径中的class对于Webapp不可见；
     *  tomcat 为了实现隔离性，没有遵守这个约定，每个webappClassLoader加载自己的目录下的class文件，不会传递给父类加载器
     *  如果不同的catalinaLoader使用双亲委派机制加载，那么不同的catalinaLoader没有办法使用不同版本的jar包，
     *  默认的累加器只看全限定名，不看版本号
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "com.cs.tomcat.catalina.Server";
        Class<?> serverClazz = commonClassLoader.loadClass(serverClassName);
        Object serverObject = serverClazz.newInstance();
        Method method = serverClazz.getMethod("start");
        method.invoke(serverObject);
    }

}
