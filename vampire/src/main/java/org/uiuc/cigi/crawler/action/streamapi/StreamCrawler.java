package org.uiuc.cigi.crawler.action.streamapi;

import org.uiuc.cigi.crawler.Crawler;
import org.uiuc.cigi.crawler.util.TokenReader;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
/**
 * abstract class for streaming api
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 9:26:08
 *
 */
public abstract class StreamCrawler implements Crawler{
	protected TwitterStream twitterStream;
	public StreamCrawler(){
		 ArrayList<ConfigurationBuilder> configList  = TokenReader.readData();
	    twitterStream = new TwitterStreamFactory(configList.get(0).build()).getInstance();
	}

}
