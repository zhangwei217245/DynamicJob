package x.spirit.dynamicjob.beak.twitter.file;


import x.spirit.dynamicjob.beak.twitter.data.TweetsCountByUsers;
import x.spirit.dynamicjob.beak.twitter.data.TwitterDataHandler;
import x.spirit.dynamicjob.core.task.type.NormalTask;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zhangwei
 */
public class TwitterFileHandler extends NormalTask<Void, String> {


    @Override
    public Void executeTask(String input) {
        Path dir = Paths.get(input);
        List<Path> subDir = new ArrayList<>();
        System.out.println("TwitterFileHandler: " + input);
        TwitterFileReader tfr = new TwitterFileReader();
        TwitterDataHandler twitterDataHandler = new TwitterDataHandler();
        TweetsCountByUsers tweetsCountByUsers = new TweetsCountByUsers();
        twitterDataHandler.setTweetsCountByUsers(tweetsCountByUsers);
        tfr.setTwitterDataHandler(twitterDataHandler);

        try {
            Files.list(dir).filter(path -> path.toFile().isDirectory()).forEach(path -> subDir.add(path));
            // work on each sub directory identified by different date
            subDir.parallelStream().forEach(path -> {
                try {
                    Files.list(path).filter(file -> file.toUri().toString().endsWith(".gz"))
                            .forEach(file -> {
                                try {
                                    Reader reader = TweetsFileFormat.getReaderBySuffix(file);
                                    System.out.println("Start to processing file: " + file.toUri());
                                    tfr.processFile(reader);
                                } catch (IOException e) {
                                    System.out.println("Exception happened on " + file);
                                    Logger.getLogger(TwitterFileHandler.class.getName()).log(Level.SEVERE, null, e);
                                }
                            });
                } catch (Throwable t) {
                    System.out.println("Exception happened on " + path);
                    Logger.getLogger(TwitterFileHandler.class.getName()).log(Level.SEVERE, null, t);
                }
                //
            });
        } catch (Throwable t) {
            t.printStackTrace();
            Logger.getLogger(TwitterFileHandler.class.getName()).log(Level.SEVERE, null, t);
        } finally {

        }

        printGridToFile(new File("/home/wesley/twitterdata/report.csv"));

        return null;
    }

    private void printGridToFile(File file) {
        try {
            file.deleteOnExit();
            file.createNewFile();
            TweetsCountByUsers.printGrid(new PrintStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
