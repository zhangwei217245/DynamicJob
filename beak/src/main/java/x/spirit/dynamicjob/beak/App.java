package x.spirit.dynamicjob.beak;

import x.spirit.dynamicjob.core.task.runner.NormalTaskRunner;
import x.spirit.dynamicjob.core.utils.ConsoleUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    private static ExecutorService executorService = null;

    public static void main( String[] args )
    {

        int corePoolSize = 1000;
        corePoolSize = Integer.valueOf(
                ConsoleUtils.readString("Please initialize thread pool with specific pool size : (default=%s)",
                        corePoolSize));

        executorService = Executors.newScheduledThreadPool(corePoolSize);
        NormalTaskRunner<Void, String> taskRunner = new NormalTaskRunner<>();
        taskRunner.setInput(ConsoleUtils.readString("Please enter the input parameter: (%s)", "path to directory"));
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
