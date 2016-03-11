package edu.ttu.geo.twitter.file;

import edu.ttu.geo.twitter.data.TweetsCountByUsers;
import edu.ttu.geo.twitter.data.TwitterDataHandler;
import edu.ttu.geo.twitter.util.Tuple;
import x.spirit.core.task.type.NormalTask;

import java.io.IOException;
import java.io.InputStream;
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
public class FileHandler extends NormalTask<Void, Tuple<String, FileFormat>>{


    @Override
    public Void executeTask(Tuple<String, FileFormat> input) {
        Path dir = Paths.get(input.getFirst());
        List<Path> subDir = new ArrayList<>();
        try {
            Files.list(dir).forEach(path -> subDir.add(path));
            // work on each sub directory identified by different date
            subDir.parallelStream().forEach(path -> {
                try {
                    Files.list(path).filter(file -> file.endsWith("txt.gz"))
                            .forEach(file -> {
                                try {
                                    InputStream inputStream = Files.newInputStream(file);
                                    Reader reader = input.getSecond().getFileReader(inputStream);
                                    TwitterFileReader tfr = new TwitterFileReader();
                                    TwitterDataHandler twitterDataHandler = new TwitterDataHandler();
                                    TweetsCountByUsers tweetsCountByUsers = new TweetsCountByUsers();
                                    twitterDataHandler.setTweetsCountByUsers(tweetsCountByUsers);
                                    tfr.setTwitterDataHandler(twitterDataHandler);
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
        }
        return null;
    }
}
