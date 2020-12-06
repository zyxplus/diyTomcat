package com.cs.tomcat.catalina.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CommonClassLoader extends URLClassLoader {
    public CommonClassLoader() {
        super(new URL[]{});

        try {
            //determine the current directory your Java application is started
            File workingFolder = new File(System.getProperty("user.dir"));
            File libFolder = new File(workingFolder, "lib");
            File[] jarFiles = libFolder.listFiles();
            for (File jarFile : jarFiles) {
                if (jarFile.getName().endsWith("jar")) {
                    URL url = new URL("file:" + jarFile.getAbsolutePath());
                    this.addURL(url);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
