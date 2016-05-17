package org.uiuc.cigi.crawler.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * class to deal with configuration file reading
 *
 * @author dawning zhzhang@cnic.cn
 **/
public class Configuration {
    private static Properties propertie;

    public static String defaultPath() {
        //String uri = Configuration.class.getResource("").toString();
        String uri = "";

        try {
            uri = ClassLoader.getSystemResource("").getFile();
            System.out.println(uri);

        } catch (Throwable e) {
            e.printStackTrace();
        }


        int num = uri.indexOf("WEB-INF");
        if (num > 0) {
            uri = uri.substring(0, num + "WEB-INF".length());
        }

        return uri;
    }


    public Configuration() {
    }

    static {
        try {
            propertie = new Properties();
            String path = defaultPath() + "config_mongo.properties";
            propertie.load(new FileInputStream(path));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String key) {
        if (propertie.containsKey(key)) {
            String value = propertie.getProperty(key);
            return value;
        } else
            return "";
    }//end getValue(...)

}
