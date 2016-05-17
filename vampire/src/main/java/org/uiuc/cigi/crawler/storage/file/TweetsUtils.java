package org.uiuc.cigi.crawler.storage.file;

import org.uiuc.cigi.crawler.config.Configuration;
import org.uiuc.cigi.crawler.util.DateFormat;
import org.uiuc.cigi.crawler.util.SystemConstant;
import twitter4j.GeoLocation;
import twitter4j.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

public class TweetsUtils {
	
	/**
	 * @param tweet
	 * @return
	 * 
	 * as the format: 
         *  0      /1   /2  /3     /4        /5         /6        /7       /8         /9
         * tweetId/text/geo/source/isretweet/search_id/create_at/meta_data/to_user_id/language_code/
         * 10     /11               /12
         * user_id/profile_image_url/user_name/
         * 13      /14        /15            /16     /17        /18            /19     /20           /21
         * place_id/place_name/place_fullname/country/place_type/street_address/boundary/boundary_type/place_url
         * 22                        /23           /24             /25            /26          /27
         * user_name(not screen name)/user_location/user_favourites/user_followers/user_friends/user_create_at
	 */
	public static String toSimpleString(Status tweet){
		StringBuffer sb = new StringBuffer();
		String sep = SystemConstant.SEPERATOR;
		
		sb.append(tweet.getId()).append(sep);
		sb.append(tweet.getCreatedAt()).append(sep);
		String text = tweet.getText();
		try {
			text = new String(text.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append(text).append(sep);
		sb.append(tweet.getUser().getLocation()).append(sep);
		String geo = null;
		if(tweet.getGeoLocation()!=null){
			geo = tweet.getGeoLocation().getLatitude()+","+tweet.getGeoLocation().getLongitude();
		}
		sb.append(geo).append(sep);
		
		sb.append(tweet.getSource()).append(sep);
		
		
		//user information
		
		sb.append(tweet.getUser().getId()).append(sep);
		sb.append(tweet.getUser().getScreenName()).append(sep);
		
		sb.append(tweet.getInReplyToUserId()).append(sep);
		sb.append(tweet.getInReplyToScreenName()).append(sep);
		
		//place information
		String placeId = null;
		String placeName = null;
		String country = null;
		String pName = null;
		String placeType = null;
		String boundary = null;
		String bType = null;
		if(tweet.getPlace()!=null){
			placeId = tweet.getPlace().getId();
			placeName = tweet.getPlace().getFullName();
			country = tweet.getPlace().getCountry();
			pName = tweet.getPlace().getName();
			placeType = tweet.getPlace().getPlaceType();
			GeoLocation[][] locations = tweet.getPlace().getBoundingBoxCoordinates();
			
			if (locations != null && locations.length > 0) {
				for (int i = 0; i < locations.length; i++) {
					for (int j = 0; locations[i].length > 0
							&& j < locations[i].length; j++) {
						boundary += "(" + locations[i][j].getLatitude() + ","
								+ locations[i][j].getLongitude() + "),";
					}
				}
			}
			bType = tweet.getPlace().getBoundingBoxType();
		}
		
		sb.append(placeId).append(sep);//place id
		sb.append(placeName).append(sep);
		sb.append(pName).append(sep);
		sb.append(country).append(sep);
		sb.append(placeType).append(sep);
		sb.append(bType).append(sep);
		sb.append(boundary);
		
		return sb.toString();
	}
	
	
	public static String toString(Status tweet){
		StringBuffer sb = new StringBuffer();
		String sep = SystemConstant.SEPERATOR;
		sb.append(tweet.getId()).append(sep);
		String text = tweet.getText().replace('\n', ' ');
		try {
			text = new String(text.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append(text).append(sep);
		String geo = null;
		if(tweet.getGeoLocation()!=null){
			geo = tweet.getGeoLocation().getLatitude()+","+tweet.getGeoLocation().getLongitude();
		}
		sb.append(geo).append(sep);
		sb.append(tweet.getSource()).append(sep);
		sb.append(tweet.isRetweet()).append(sep);
		sb.append("1").append(sep);
		sb.append(tweet.getCreatedAt()).append(sep);
		sb.append("").append(sep);
		sb.append(tweet.getInReplyToUserId()).append(sep);
		sb.append(tweet.getUser().getLang()).append(sep);
		sb.append(tweet.getUser().getId()).append(sep);
		sb.append(tweet.getUser().getProfileBackgroundImageUrl()).append(sep);
		sb.append(tweet.getUser().getScreenName()).append(sep);
			
		//user information
		/**
		 * obj.put("user_sname", user.getScreenName());
			obj.put("user_name", user.getName());
			obj.put("user_location", user.getLocation());
			obj.put("num_favorite", user.getFavouritesCount());
			obj.put("num_followers", user.getFollowersCount());
			obj.put("num_friends", user.getFriendsCount());
			obj.put("user_createAt", user.getCreatedAt());
			obj.put("num_status", user.getStatusesCount());
		 */
		
		/*
		sb.append(tweet.getInReplyToUserId()).append(sep);
		sb.append(tweet.getInReplyToScreenName()).append(sep);
		*/
		//place information
		String placeId = null;
		String placeName = null;
		String country = null;
		String pName = null;
		String placeType = null;
		String streetAddress = null;
		String boundary = "";
		String bType = null;
		String place_url = null;
		if(tweet.getPlace()!=null){
			placeId = tweet.getPlace().getId();
			placeName = tweet.getPlace().getFullName();
			country = tweet.getPlace().getCountry();
			pName = tweet.getPlace().getName();
			streetAddress = tweet.getPlace().getStreetAddress();
			placeType = tweet.getPlace().getPlaceType();
			place_url = tweet.getPlace().getURL();
			GeoLocation[][] locations = tweet.getPlace().getBoundingBoxCoordinates();
			
			if (locations != null && locations.length > 0) {
				for (int i = 0; i < locations.length; i++) {
					for (int j = 0; locations[i].length > 0
							&& j < locations[i].length; j++) {
						boundary += "(" + locations[i][j].getLatitude() + ","
								+ locations[i][j].getLongitude() + "),";
					}
				}
			}
			bType = tweet.getPlace().getBoundingBoxType();
		}
		
		sb.append(placeId).append(sep);//place id
		sb.append(pName).append(sep);
		sb.append(placeName).append(sep);
		sb.append(country).append(sep);
		sb.append(placeType).append(sep);
		sb.append(streetAddress).append(sep);
		sb.append(boundary).append(sep);
		sb.append(bType).append(sep);
		sb.append(place_url).append(sep);
		sb.append(tweet.getUser().getName()).append(sep);
		sb.append(tweet.getUser().getLocation()).append(sep);
		sb.append(tweet.getUser().getFavouritesCount()).append(sep);
		sb.append(tweet.getUser().getFollowersCount()).append(sep);
		sb.append(tweet.getUser().getFriendsCount()).append(sep);
		sb.append(tweet.getUser().getCreatedAt());
		
		return sb.toString();
	}
	/**
	 * write to file with the name format "date_sim.txt"
	 * @param tweets
	 */
	public static void writeToFile(List<Status> tweets){
		File dir = new File(Configuration.getValue("DataDirectory"));
		File todayFile = null;
		if(dir.exists()&&dir.isDirectory()){
			Date today = new Date();
			String day = DateFormat.convertDate(today, "yyyy_MM_dd");
			todayFile = new File(dir.getPath()+File.separator+day+"_search.txt");
			if(!todayFile.exists()){
				try {
					todayFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(todayFile.getPath(),true);
			StringBuffer sb = new StringBuffer();
			for(Status tweet : tweets){
				sb.append(toString(tweet)).append("\n");
			}
			fos.write(sb.toString().getBytes());
		} catch (Exception e1) {
			e1.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void writeStatusToFile(List<Status> statuses){
		File dir = new File(Configuration.getValue("DataDirectory"));
		File todayFile = null;
                File readStatusFile = null;
                File tempFile = null;
		if(dir.exists()&&dir.isDirectory()){
			Date today = new Date();
			String day = DateFormat.convertDate(today, "yyyy_MM_dd");
			todayFile = new File(dir.getPath()+File.separator+day+"_stream.txt");
			tempFile = new File(dir.getPath()+File.separator+"temp.txt");
			readStatusFile = new File(dir.getPath()+File.separator+"status.txt");
			if(!todayFile.exists()){
				try {
					todayFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(!tempFile.exists()){
				try {
					tempFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(!readStatusFile.exists()){
				try {
					readStatusFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(todayFile.getPath(),true);
			StringBuffer sb = new StringBuffer();
			for(Status tweet : statuses){
				sb.append(toString(tweet)).append("\n");
			}
			fos.write(sb.toString().getBytes());
		} catch (Exception e1) {
			e1.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
