package x.spirit.dynamicjob.beak.twitter.file;


import x.spirit.dynamicjob.beak.twitter.data.TweetsCountByUsers;
import x.spirit.dynamicjob.beak.twitter.data.TwitterDataHandler;
import x.spirit.dynamicjob.core.task.type.NormalTask;

import java.io.IOException;
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
public class FileHandler extends NormalTask<Void, String> {


    @Override
    public Void executeTask(String input) {
        Path dir = Paths.get(input);
        List<Path> subDir = new ArrayList<>();

        TwitterFileReader tfr = new TwitterFileReader();
        TwitterDataHandler twitterDataHandler = new TwitterDataHandler();
        TweetsCountByUsers tweetsCountByUsers = new TweetsCountByUsers();
        twitterDataHandler.setTweetsCountByUsers(tweetsCountByUsers);
        tfr.setTwitterDataHandler(twitterDataHandler);

        try {
            Files.list(dir).forEach(path -> subDir.add(path));
            // work on each sub directory identified by different date
            subDir.parallelStream().forEach(path -> {
                try {
                    Files.list(path).filter(file -> file.endsWith("txt.gz"))
                            .forEach(file -> {
                                try {
                                    Reader reader = FileFormat.getReaderBySuffix(file);
                                    System.out.println("Start to processing file: " + file.toUri());
                                    tfr.processFile(reader);
                                } catch (IOException e) {
                                    Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, e);
                                }
                            });
                } catch (IOException e) {
                    Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, e);
                }
            });
        } catch (IOException e) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            tweetsCountByUsers.printGrid();
        }
        return null;
    }
}
