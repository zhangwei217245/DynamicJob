package x.spirit.core;

import edu.ttu.geo.twitter.file.FileHandler;
import x.spirit.core.task.runner.NormalTaskRunner;

import java.io.Console;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author zhangwei
 */
public class App {

    private static ExecutorService executorService = null;

    public static void main(String[] args) {
        int corePoolSize = 1000;
        corePoolSize = Integer.valueOf(
                readString("Please initialize thread pool with specific pool size : (default=%s)",
                        corePoolSize));

        executorService = Executors.newScheduledThreadPool(corePoolSize);
        NormalTaskRunner<Void, String> taskRunner = new NormalTaskRunner<>();
        taskRunner.setInput(readString("Please enter the input parameter: (%s)", "path to directory"));
        taskRunner.setTaskClass(FileHandler.class);
        executorService.submit(taskRunner);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
        });

    }

    private static String readPassword(String promptFmt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return new String(console.readPassword(promptFmt, args));
    }

    private static String readString(String prompt, Object... args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console is not available right now.");
        }
        return console.readLine(prompt, args);
    }
}
