package x.spirit.dynamicjob.beak;

import x.spirit.dynamicjob.beak.twitter.file.TwitterFileHandler;
import x.spirit.dynamicjob.core.task.runner.NormalTaskRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 *
 */
public class App 
{
    private static ExecutorService executorService = null;

    public static void main( String[] args )
    {

        int corePoolSize = 20;
        //corePoolSize = Integer.valueOf(
        //        ConsoleUtils.readString("Please initialize thread pool with specific pool size : (default=%s)",
        //                corePoolSize));

        executorService = Executors.newFixedThreadPool(corePoolSize);
        NormalTaskRunner<Void, String> taskRunner = new NormalTaskRunner<>();
        String dirPath = "/home/wesley/twitterdata";//ConsoleUtils.readString("Please enter the input parameter: (%s)", "path to directory");
        taskRunner.setInput(dirPath);
        taskRunner.setTaskClass(TwitterFileHandler.class);
        System.out.println("Ready to submit task:");
        //executorService.submit(taskRunner);
        try {
            taskRunner.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Task submitted!");

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
