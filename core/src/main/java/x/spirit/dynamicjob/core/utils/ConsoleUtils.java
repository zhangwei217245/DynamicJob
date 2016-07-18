package x.spirit.dynamicjob.core.utils;

import java.io.Console;
import java.util.Scanner;

/**
 * Created by zhangwei on 3/15/16.
 */
public class ConsoleUtils {

    public static String readString(String promptFmt, Object... args) {
        System.out.println(String.format(promptFmt, args));
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    public static String readPassword(String promptFmt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return new String(console.readPassword(promptFmt, args));
    }

    public static String readString2(String prompt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return console.readLine(prompt, args);
    }

    public static void main(String[] args) {
        String s = "A função, Ãugent!_{10028.2al-ksjd}";
        String r = s.replaceAll("[^a-zA-Z\\s\\.\\_\\-]", "").replace('-',' ').replace('_',' ');
        System.out.println(r);
    }
}
