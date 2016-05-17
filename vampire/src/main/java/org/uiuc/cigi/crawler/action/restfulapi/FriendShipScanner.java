package org.uiuc.cigi.crawler.action.restfulapi;


import org.apache.log4j.Logger;
import org.uiuc.cigi.crawler.util.TokenReader;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;

public class FriendShipScanner {
	static Logger log = Logger.getLogger(FriendShipScanner.class);
	static Twitter twitter = null;
	static int tokenIndex = 0;
	
	
	private static void setOauthTwitter(){
		 ArrayList<ConfigurationBuilder> configList  = TokenReader.readData();
	     twitter = new TwitterFactory(configList.get(0).build()).getInstance();
	}
	
	public static void changeTwitter(int i){
		ArrayList<ConfigurationBuilder> configList  = TokenReader.readData();
		twitter = new TwitterFactory(configList.get(i%configList.size()).build()).getInstance();
		
	}
	
	public static int getRemainingHits(){
		try {
			if(twitter==null){
				setOauthTwitter();
			}
			return twitter.getRateLimitStatus().getRemainingHits();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static IDs getFollowers(long userId){
		if(twitter==null){
			setOauthTwitter();
		}
		try {
			return twitter.getFollowersIDs(userId, -1);
		} catch (TwitterException e) {
			log.warn(userId + " is not a public guy");
			e.printStackTrace();
		}
		return null;
	}
	
	public static IDs getFriends(long userId){
		if(twitter==null){
			setOauthTwitter();
		}
		try {
			return twitter.getFriendsIDs(userId, -1);
		} catch (TwitterException e) {
			log.warn(userId + " is not a public guy");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
