package x.spirit.dynamicjob.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangwei on 7/19/16.
 */
public class StringUtils {

    public static String removeUrl(String document){
        String str = document;
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+" +
                "[\\w\\d:#@%/;\\$\\(\\)~_\\?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(),"").trim();
        }
        return str;
    }
}
