package edu.ttu.geo.twitter.file;

import edu.ttu.geo.twitter.data.TwitterDataHandler;
import edu.ttu.geo.twitter.util.Tuple;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.Reader;

/**
 * 
 * @author zhangwei
 */
public class TwitterFileReader {

    private TwitterDataHandler twitterDataHandler;

    /**
     * Open a file and read it line by line while transforming lines into certain
     * format that is ready for the next processing step.
     * @param reader  The file reader.
     */
    public void processFile(Reader reader) {

        BufferedReader in = new BufferedReader(reader);
        in.lines().map(line -> getStatusFromLine(line))
                .forEach(data -> twitterDataHandler.handleTwitterData(data));

    }

    /**
     * Convert Each line into a tuple which contains a Status Object along with its timestamp.
     * @param line
     * @return
     */
    private Tuple<Status, Long> getStatusFromLine(String line) {
        try {
            String[] pair = line.split("\\|");
            long timestamp = Long.valueOf(pair[0]);
            String statusJson = pair[1];
            Status status = TwitterObjectFactory.createStatus(statusJson);
            return new Tuple<>(status, timestamp);
        } catch (TwitterException e) {
            return null;
        }
    }

    public void setTwitterDataHandler(TwitterDataHandler twitterDataHandler) {
        this.twitterDataHandler = twitterDataHandler;
    }
}
