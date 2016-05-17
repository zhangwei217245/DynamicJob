package org.uiuc.cigi.crawler.storage.db;

import twitter4j.Status;

import java.util.List;

public interface TweetDao {
	
	/**
	 * add tweets to database
	 * @param statuses
	 */
	public void addTweets(List<Status> statuses);
	
	/**
	 * add tweets collected by search api to database 
	 * the schema is different from streaming apis
	 * @param statuses
	 */
	public void addSearchTweets(List<Status> statuses);
}
