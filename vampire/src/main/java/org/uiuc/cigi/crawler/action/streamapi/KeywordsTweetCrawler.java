package org.uiuc.cigi.crawler.action.streamapi;

import org.uiuc.cigi.crawler.action.streamapi.listener.BatchStatusListener;

import twitter4j.FilterQuery;

/**
 * retrieve keywords related tweets from Twitter
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 9:35:03
 *
 */
public class KeywordsTweetCrawler extends StreamCrawler{
	String[] keywords;
	public KeywordsTweetCrawler(String[] keywords){
		super();
		this.keywords = keywords;
	}
	
	public void collect() {
		twitterStream.addListener(new BatchStatusListener(keywords,false));
	     // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
	    twitterStream.filter(new FilterQuery(0, null, keywords,null));
	}

	public void collect(int buffersize) {
		twitterStream.addListener(new BatchStatusListener(keywords,false));
	     // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
	    twitterStream.filter(new FilterQuery(0, null, keywords,null));
	}
}
