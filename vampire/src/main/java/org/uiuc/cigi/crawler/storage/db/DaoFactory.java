package org.uiuc.cigi.crawler.storage.db;



public class DaoFactory {
  public static TweetDao getTweetDao(){
	return new TweetDaoImpl();
}
  
}
