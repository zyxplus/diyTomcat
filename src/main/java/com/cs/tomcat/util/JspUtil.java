package com.cs.tomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.tomcat.catalina.Context;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;

import java.io.File;

public class JspUtil {
    private static final String javaKeywords[] = {"abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile",
            "while"};

    public static void main(String[] args) {
        try {
            Context context = new Context("/Users/pc/Desktop/ZYX/kJava/PROJECT_SEVEN/diyTomcat/web", null, null, true);
            File file = new File("/Users/pc/Desktop/ZYX/kJava/PROJECT_SEVEN/diyTomcat/web/index.jsp");
            compileJsp(context, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compileJsp(Context context, File file) throws JasperException {
        String subFolder;
        String path = context.getPath();
        if ("/".equals(path)) {
            subFolder = "_";
        } else {
            subFolder = StrUtil.subAfter(path, '/', false);
        }

        //-webapp: docBase指向了你某个应用的目录
        //-d: 工作路径
        String workPath = new File(Constant.WORKFOLDER, subFolder).getAbsolutePath() + File.separator;
        String[] args = new String[]{"-webapp", context.getDocBase().toLowerCase(),
                "-d", workPath.toLowerCase(),
                "-compile"};
        JspC jspC = new JspC();
        jspC.setArgs(args);
        jspC.execute(file);
    }

    /**
     * Converts the given identifier to a legal Java identifier.
     *
     * @param identifier
     * @return
     */
    public static final String makeJaveIdentifier(String identifier) {
        return makeJaveIdentifier(identifier, true);
    }

    public static final String makeJaveIdentifier(String identifier, boolean periodToUnderscore) {
        StringBuilder modifiedIdentifier = new StringBuilder(identifier.length());
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            modifiedIdentifier.append("_");
        }
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (Character.isJavaIdentifierPart(ch) && (ch != '_' || !periodToUnderscore)) {
                modifiedIdentifier.append(ch);
            } else if (ch == '.' && periodToUnderscore) {
                modifiedIdentifier.append("_");
            } else {
                modifiedIdentifier.append(mangleChar(ch));
            }
        }
        if (isJavaKeyword(modifiedIdentifier.toString())) {
            modifiedIdentifier.append("_");
        }
        return modifiedIdentifier.toString();

    }

    private static boolean isJavaKeyword(String key) {
        int i = 0;
        int j = javaKeywords.length;
        while (i < j) {
            int k = (i + j) / 2;
            int result = javaKeywords[k].compareTo(key);
            if (result == 0) {
                return true;
            }
            if (result < 0) {
                i = k + 1;
            } else {
                j = k;
            }
        }
        return false;
    }


    /**
     * Mangle the specified character to create a legal Java class name.
     *
     * @param ch
     * @return the replacement character as a string
     */
    private static String mangleChar(char ch) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit((ch >> 12) & 0xf, 16);
        result[2] = Character.forDigit((ch >> 8) & 0xf, 16);
        result[3] = Character.forDigit((ch >> 4) & 0xf, 16);
        result[4] = Character.forDigit(ch & 0xf, 16);
        return new String(result);
    }


    public static String getServletPath(String uri, String subFolder) {
        String tempPath = "org/apache/jsp/" + uri;

        File tempFile = FileUtil.file(Constant.WORKFOLDER, subFolder, tempPath);
        String fileNameOnly = tempFile.getName();
        String classFileName = JspUtil.makeJaveIdentifier(fileNameOnly);

        File servletFile = new File(tempFile.getParent(), classFileName);
        return servletFile.getAbsolutePath();
    }

    public static String getServletClassPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".class";
    }

    public static String getServletJavaPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".java";
    }

    public static String getJspServletClassName(String uri, String subFolder) {
        File tempFile = FileUtil.file(Constant.WORKFOLDER, subFolder);
        String tempPath = tempFile.getAbsolutePath() + File.separator;
        String servletPath = getServletPath(uri, subFolder);

        String jsServletClassPath = StrUtil.subAfter(servletPath, tempPath, false);
        String jspServletClassName = StrUtil.replace(jsServletClassPath, File.separator, ".");
        return jspServletClassName;
    }

}


