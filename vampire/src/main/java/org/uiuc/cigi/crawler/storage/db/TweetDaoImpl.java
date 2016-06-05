package org.uiuc.cigi.crawler.storage.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TweetDaoImpl implements TweetDao {
	Logger log = Logger.getLogger(this.getClass());
	public static String sep = String.valueOf((char)1);
	
	public void addTweets(List<Status> statuses) {
		DB db = DB_Util.getConnection();
		//get connection to specific collections from the connection pool 
		DBCollection yardColl = db.getCollection("tweet_yard");
		List<DBObject> objList = new ArrayList<DBObject>();
		for (Status status : statuses) {
			// add places
			BasicDBObject obj = new BasicDBObject();
			obj.put("tweetId", status.getId());
			
			String tweet = status.getText();
			try {
				tweet = new String(tweet.getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			obj.put("tweet", tweet);
			
			if (status.getGeoLocation()!= null) {
				double[] geoArray = new double[2];
				geoArray[0] = status.getGeoLocation().getLongitude();
				geoArray[1] = status.getGeoLocation().getLatitude();
				obj.put("geo", geoArray);
			} else {
				obj.put("geo", null);
			}
			obj.put("source", status.getSource());
			obj.put("isretweet", status.isRetweet());
			obj.put("create_at", status.getCreatedAt());
			obj.put("to_user_id", status.getInReplyToUserId());
			
			// add accounts
			User user = status.getUser();
			obj.put("user_id", user.getId());
			obj.put("user_sname", user.getScreenName());
			obj.put("user_name", user.getName());
			obj.put("user_location", user.getLocation());
			obj.put("num_favorite", user.getFavouritesCount());
			obj.put("num_followers", user.getFollowersCount());
			obj.put("num_friends", user.getFriendsCount());
			obj.put("user_createAt", user.getCreatedAt());
			obj.put("num_status", user.getStatusesCount());
			//add place
			Place place = status.getPlace();
			if (place != null) {
				obj.put("place_id", place.getId());
				obj.put("place_name", place.getName());
				obj.put("place_fullname", place.getFullName());
				obj.put("country", place.getCountry());
				obj.put("place_type", place.getPlaceType());
				
				GeoLocation [][] locations = place.getBoundingBoxCoordinates();
				String boundary = "";
				if(locations!=null&&locations.length>0){
					for(int i=0;i<locations.length;i++){
						for(int j=0;locations[i].length>0&&j<locations[i].length;j++){
							boundary += "("+locations[i][j].getLatitude()+","+locations[i][j].getLongitude()+"),";
						}
					}
				}
				obj.put("boundary", boundary);
				obj.put("boundary_type",place.getBoundingBoxType());
			}
			
			// add the single tweet object into cache List
			objList.add(obj);
			
		}
		// batch the cached tweets into database
		yardColl.insert(objList);
	}

	public void addSearchTweets(List<Status> tweets) {
		DB db = DB_Util.getConnection();
		//get connection to specific collections from the connection pool 
		DBCollection yardColl = db.getCollection("tweet_yard_search");
		List<DBObject> objList = new ArrayList<DBObject>();
		for (Status status : tweets) {
			// add places
			BasicDBObject obj = new BasicDBObject();
			obj.put("tweetId", status.getId());
			
			String tweet = status.getText();
			try {
				tweet = new String(tweet.getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			obj.put("tweet", tweet);
			
			if (status.getGeoLocation()!= null) {
				double[] geoArray = new double[2];
				geoArray[0] = status.getGeoLocation().getLongitude();
				geoArray[1] = status.getGeoLocation().getLatitude();
				obj.put("geo", geoArray);
			} else {
				obj.put("geo", null);
			}
			obj.put("source", status.getSource());
			obj.put("create_at", status.getCreatedAt());
			obj.put("to_user_id", status.getInReplyToUserId());
			
			// add accounts
			obj.put("user_id", status.getUser().getId());
			obj.put("user_name", status.getUser().getScreenName());
			obj.put("user_location", status.getUser().getLocation());
			//add place
			Place place = status.getPlace();
			if (place != null) {
				obj.put("place_id", place.getId());
				obj.put("place_name", place.getName());
				obj.put("place_fullname", place.getFullName());
				obj.put("country", place.getCountry());
				obj.put("place_type", place.getPlaceType());
				
				GeoLocation [][] locations = place.getBoundingBoxCoordinates();
				String boundary = "";
				if(locations!=null&&locations.length>0){
					for(int i=0;i<locations.length;i++){
						for(int j=0;locations[i].length>0&&j<locations[i].length;j++){
							boundary += "("+locations[i][j].getLatitude()+","+locations[i][j].getLongitude()+"),";
						}
					}
				}
				obj.put("boundary", boundary);
				obj.put("boundary_type",place.getBoundingBoxType());
			}
			
			// add the single tweet object into cache List
			objList.add(obj);
			
		}
		// batch the cached tweets into database
		yardColl.insert(objList);
	}
}
