package org.uiuc.cigi.crawler.action.streamapi.listener;

import org.apache.log4j.Logger;
import org.uiuc.cigi.crawler.config.Configuration;
import org.uiuc.cigi.crawler.storage.file.TweetsUtils;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.ArrayList;
import java.util.List;

public class BatchStatusListener implements StatusListener{
	 private String[] keywords;
	 
	 /**
	  * the flag field to tell whether we should find keyword related tweets
	  */
	 private boolean isFiltered;
	 
         private int buffersize;	
 
	 private List<Status> statuses;
	 Logger log = Logger.getLogger(this.getClass());
     public BatchStatusListener(String[] keywords	, boolean isFiltered) {
		super();
		this.keywords = keywords;
		statuses = new ArrayList<Status>();
		this.isFiltered = isFiltered;
	}
    /**
     * whether the keywords are contained in  tweet 
     * @return
     */
     private boolean isInTweet(String tweet){
    	 for(String keyword	: keywords){
    		 if(tweet.toLowerCase().contains(keyword.toLowerCase())){
    			 return true;
    		 }
    	 }
    	 return false;
     }

     public boolean setBuffersize(int size){
         buffersize = size;
         return true;
     }
     
     public void onStatus(Status status){
    		 
    	 if(isFiltered){
    		 if(keywords!=null && !isInTweet(status.getText())){
    			return;
    		 }
    	 }
    	 if(status.getUser()!=null){
    		 
    	 }else{
    		 log.warn(status.getId()+" does not have a user field");
    		 return;
    	 }
    	 
    	 statuses.add(status);
         //log.info(statuses.size());
         if (buffersize <= 0){
                 buffersize = Integer.parseInt(Configuration.getValue("BufferSize"));
         }
    	 if(statuses.size() == buffersize){
    		 // write to cache files with date related file name
    		 TweetsUtils.writeStatusToFile(statuses);
    		 log.info(statuses.size() + "tweets have been put into today's file");
    	/*	 // write to MongoDB
    		 TweetDao td = DaoFactory.getTweetDao();
    		 td.addTweets(statuses);
    		 log.info(statuses.size() + "tweets have been put into database tweet_yard");
        */ 
   		 statuses.clear();
    	 }
     }
	
	
     public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
         System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
         log.info("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
     }

     public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
         System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
         log.info("Got track limitation notice:" + numberOfLimitedStatuses);
     }

     public void onScrubGeo(long userId, long upToStatusId) {
         System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
         log.info("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
     }


	public void onException(Exception ex) {
         ex.printStackTrace();
     }
 };
