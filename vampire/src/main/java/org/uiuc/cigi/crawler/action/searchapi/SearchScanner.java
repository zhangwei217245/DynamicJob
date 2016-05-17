package org.uiuc.cigi.crawler.action.searchapi;

import java.util.List;

import org.apache.log4j.Logger;
import org.uiuc.cigi.crawler.storage.db.DaoFactory;
import org.uiuc.cigi.crawler.storage.db.TweetDao;
import org.uiuc.cigi.crawler.storage.file.TweetsUtils;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class SearchScanner {
	Query query = null;
	Twitter twitter = null;
	Logger log = Logger.getLogger(this.getClass());
	
	public SearchScanner(String queryString){
		twitter = new TwitterFactory().getInstance();
		query = new Query();
		if(queryString.indexOf("&")>-1){
			String[] segs = queryString.split("&");
			for(String seg : segs){
				if(seg.startsWith("q=")){
					String temp = seg.substring(2);
					query.setQuery(temp);
				}else if(seg.startsWith("geocode=")){
					String temp = seg.substring(8);
					String [] temps = temp.split(",");
					if(temps.length==3){
						double lat = Double.valueOf(temps[0]);
						double lng = Double.valueOf(temps[1]);
						String units = temps[2].substring(temps[2].length()-2);
						double miles = Double.valueOf(temps[2].substring(0,temps[2].length()-2));
						query.geoCode(new GeoLocation(lat , lng), miles, units);
					}
				}else if(seg.startsWith("count=")){
					String temp = seg.substring(6);
					query.setRpp(Integer.parseInt(temp));
				}else if(seg.startsWith("page=")){
					String temp = seg.substring(5);
					query.setPage(Integer.parseInt(temp));
				}
			}
		}
	}
	
	// initialize a search crawler instance
	public SearchScanner(Twitter twitter , long sinceId , String keywords){
		this.twitter = twitter;
		query = new Query();
		query.setRpp(100);
		query.setSinceId(sinceId);
		if(keywords!=null&&!"".equals(keywords)){
			query.setQuery(keywords);
		}
			
	}
	// update the query
	public void setQuery(Region region , int page){
		query.geoCode(new GeoLocation(region.getLatitude(),region.getLongitude()), region.getDistance(), region.getUnits());
		query.setPage(page);
	}
	
	/**
	 * search a page and return the largest id
	 * @return
	 */
	public TempSearchResult search(){
		TempSearchResult r = new TempSearchResult();
		try {
			QueryResult result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			// write to file
			TweetsUtils.writeToFile(tweets);
			
			// import into db
			TweetDao td = DaoFactory.getTweetDao();
			td.addSearchTweets(tweets);
			
			r.maxId = tweets.get(tweets.size()-1).getId();
			if(tweets.size()<100){
				r.flag = false;
			}else {
				r.flag = true;
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return r;
	}
	//get tweets amount for every page to see whether it is larger than 100
	public int getResultsAmounts(){
		try {
			QueryResult result = twitter.search(query);
			return result.getTweets().size();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	

}
