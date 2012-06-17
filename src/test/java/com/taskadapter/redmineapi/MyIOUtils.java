package com.taskadapter.redmineapi;

import java.io.*;

public class MyIOUtils {

    public static InputStream getResourceAsStream(String resource)
            throws IOException {
        ClassLoader cl = MyIOUtils.class.getClassLoader();
        InputStream in = cl.getResourceAsStream(resource);

        if (in == null) {
            throw new IOException("resource \"" + resource + "\" not found");
        }

        return in;
    }

    /**
     * Loads the resource from classpath
     */
    public static String getResourceAsString(String resource)
            throws IOException {
        InputStream in = getResourceAsStream(resource);
        return convertStreamToString(in);
    }

    private static String convertStreamToString(InputStream is)
            throws IOException {
        /*
           * To convert the InputStream to String we use the Reader.read(char[]
           * buffer) method. We iterate until the Reader return -1 which means
           * there's no more data to read. We use the StringWriter class to
           * produce the string.
           */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
