package org.uiuc.cigi.crawler.action.restfulapi;

import org.uiuc.cigi.crawler.Crawler;
/**
 * deal with friendship graph of twitter
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 4:41:27
 *
 */
public class FriendShipCrawler implements Crawler{
	String storedFile;
	public FriendShipCrawler(String storedFile){
		this.storedFile = storedFile;
		
	}
	public void collect() {
		FriendshipMap.getFriendshipMap(storedFile);
	}

	public void collect(int buffersize) {
		FriendshipMap.getFriendshipMap(storedFile);
	}
}
