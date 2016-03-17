package x.spirit.dynamicjob.beak.twitter.file;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import x.spirit.dynamicjob.beak.twitter.data.TwitterDataHandler;
import x.spirit.dynamicjob.beak.twitter.util.Tuple;

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
        in.lines().filter(line -> line.contains("|")).forEach(line -> {
                twitterDataHandler.handleTwitterData(getStatusFromLine(line));

        });
        //.filter(tuple -> tuple != null ).forEach(data -> twitterDataHandler.handleTwitterData(data));

    }

    /**
     * Convert Each line into a tuple which contains a Status Object along with its timestamp.
     * @param line
     * @return
     */
    private Tuple<String, Long> getStatusFromLine(String line) {
        try{
            String[] pair = line.split("\\|");
            long timestamp = Long.valueOf(pair[0]);
            String statusJson = pair[1];
            Status status = TwitterObjectFactory.createStatus(statusJson);
            return new Tuple<>(statusJson, timestamp);
        } catch (Throwable t) {
            System.out.println(" ===== "+ t.getMessage() +"====\n" +  line + "\n===== "+ t.getMessage() +"====");
            return null;
        }
    }

    public void setTwitterDataHandler(TwitterDataHandler twitterDataHandler) {
        this.twitterDataHandler = twitterDataHandler;
    }
}
