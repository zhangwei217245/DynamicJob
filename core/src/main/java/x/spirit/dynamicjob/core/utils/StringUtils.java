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
                "[\\w\\d:#\\@%/;\\$\\(\\)~_\\?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(),"").trim();
        }
        return str;
    }
    public static String removeHashTag(String document){
        String str = document;
        String urlPattern = "(#[_\\-\\w\\d]+)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(),"").trim();
        }
        return str;
    }

    public static String removeMemtion(String document){
        String str = document;
        String urlPattern = "(\\@[_\\-\\w\\d]+)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(),"").trim();
        }
        return str;
    }

    public static String removeNonAlphabet(String str) {
        return str.replaceAll("([^a-zA-Z\\d\\s\\.,\\-_#@])","");
    }

    public static void main(String[] args) {
        String a = "hash-tag #playfi2010_220 url,. http://soiuoiu.com mention @romoa [_] {}中文";
        System.out.println(removeHashTag(a));
        System.out.println(removeMemtion(a));
        System.out.println(removeUrl(a));
        System.out.println(removeHashTag(removeMemtion(removeUrl(a))));
        System.out.println(removeNonAlphabet(a));
    }
}
