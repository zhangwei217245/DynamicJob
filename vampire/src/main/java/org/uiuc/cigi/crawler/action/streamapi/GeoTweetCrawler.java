package org.uiuc.cigi.crawler.action.streamapi;

import org.uiuc.cigi.crawler.action.streamapi.listener.BatchStatusListener;

import twitter4j.FilterQuery;

/**
 * get geo based tweets from twitter platform
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 9:27:49
 *
 */
public class GeoTweetCrawler extends StreamCrawler{
	// like { {-132.48, 13.21}, {-54.96, 59.07}}; two dimension array to specify the bounding box
	double[][] boundary;
	String[] keywords;
	public GeoTweetCrawler(double[][] boundary , String[] keywords){
		super();
		this.boundary = boundary;
		this.keywords = keywords;
	}
	
	public void collect() {
		twitterStream.addListener(new BatchStatusListener(keywords,true));
	     // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
	    twitterStream.filter(new FilterQuery(0, null, keywords,boundary));
	}

	public void collect(int buffersize) {
			BatchStatusListener listener = new BatchStatusListener(keywords,true);
			listener.setBuffersize(buffersize);
			twitterStream.addListener(listener);
		 // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
		twitterStream.filter(new FilterQuery(0, null, keywords,boundary));
	}

}
