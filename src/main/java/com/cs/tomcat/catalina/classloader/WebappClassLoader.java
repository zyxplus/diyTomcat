package com.cs.tomcat.catalina.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * WebappClassLoader：各个Webapp私有的类加载器，加载路径中的class只对当前Webapp可见；
 */
public class WebappClassLoader extends URLClassLoader {

    /**
     * 每个webappClassLoader加载自己的目录下的class文件，不会传递给父类加载器
     * @param docBase
     * @param commonClassLoader
     */
    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);

        try {
            File webinfFolder = new File(docBase, "WEB-INF");
            File classFolder = new File(webinfFolder, "classes");
            File libFolder = new File(webinfFolder, "lib");
            URL url;
            url = new URL("file:" + classFolder.getAbsolutePath() + "/");
            this.addURL(url);
            List<File> jarFiles = FileUtil.loopFiles(libFolder);
            for (File jarFile : jarFiles) {
                url = new URL("file:" + jarFile.getAbsolutePath());
                this.addURL(url);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
