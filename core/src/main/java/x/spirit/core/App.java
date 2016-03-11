package x.spirit.core;

import edu.ttu.geo.twitter.file.FileHandler;
import x.spirit.core.task.runner.NormalTaskRunner;

import java.util.Scanner;
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

        System.out.println("Please initialize thread pool with specific pool size:");
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            corePoolSize = Integer.valueOf(scanner.nextLine());
        }
        executorService = Executors.newScheduledThreadPool(corePoolSize);

        NormalTaskRunner<Void, String> taskRunner = new NormalTaskRunner<>();
        System.out.println("Please enter the input parameter:");
        scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            taskRunner.setInput(scanner.nextLine());
        }
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
}
