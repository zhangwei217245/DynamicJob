package x.spirit.dynamicjob.core.utils;

import java.io.Console;

/**
 * Created by zhangwei on 3/15/16.
 */
public class ConsoleUtils {

    public static String readPassword(String promptFmt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return new String(console.readPassword(promptFmt, args));
    }

    public static String readString(String prompt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return console.readLine(prompt, args);
    }
}
