package org.uiuc.cigi.crawler.action.restfulapi;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uiuc.cigi.crawler.storage.file.FileWriter;
import org.uiuc.cigi.crawler.util.SystemConstant;
import twitter4j.IDs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class FriendshipMap {
	static Logger log = Logger.getLogger(FriendShipScanner.class);
	public static void getFriendshipMap(String filePath){
		Queue<Long> processingQueue = new ConcurrentLinkedQueue<Long>();
		List<Long> processedList = new ArrayList<Long>();
		FriendShipScanner.tokenIndex = 0;
		//begin crawler
		processingQueue.add(SystemConstant.StartPointId);
		while(!processingQueue.isEmpty()){
			Long head = processingQueue.poll();
			int hints = FriendShipScanner.getRemainingHits();
			log.info("remaining hits reach "+hints+" change accounts");
			if(hints<4){
				FriendShipScanner.changeTwitter(++FriendShipScanner.tokenIndex);
				log.info("change accounts to "+FriendShipScanner.tokenIndex);
			}
			hints =  FriendShipScanner.getRemainingHits();
			IDs friends = FriendShipScanner.getFriends(head);
			IDs followers = FriendShipScanner.getFollowers(head);
//			log.info("has processed "+ (++processCount));
			if(friends==null&&followers==null){
				processData(filePath,head, null,null);
				continue;
			}
			
			
			
			long[] friendsArray = friends.getIDs();
			long[] followerArray = followers.getIDs();
		
			processData(filePath,head, friendsArray,followerArray);
			if(friendsArray!=null&&friendsArray.length>0){
				for(long friend : friendsArray){
					if(!processingQueue.contains(friend)){
						processingQueue.add(friend);
					}
				}
			}
			if(followerArray!=null&&followerArray.length>0){
				for(long follower : followerArray){
					if(!processingQueue.contains(follower)){
						processingQueue.add(follower);
					}
				}
			}
			// add the processed id into processed list
			if(!processedList.contains(head)){
				processedList.add(head);
			}	
		}
	}

	
	public static void processData(String filePath , long userId, long[] friends){
		String content = userId + ":";
		for(int i=0;i<friends.length;i++){
			content += friends[i];
			if(i!=friends.length-1){
				content += ",";
			}
		}
		content+='\n';
		FileWriter.writeIntoFile(filePath, content, true);
	}
	
	public static void processData(String filePath , long userId, long[] friends, long[] followers){
		String content = userId + ":";
		if(friends!=null&&friends.length>0){
			for(int i=0;i<friends.length;i++){
				content += friends[i];
				if(i!=friends.length-1){
					content += ",";
				}
			}
		}
		
		content += "\n";
		content += userId + ":";
		if(followers!=null && followers.length>0){
			for(int i=0;i<followers.length;i++){
				content += followers[i];
				if(i!=followers.length-1){
					content += ",";
				}
			}
		}
		content+='\n';
		FileWriter.writeIntoFile(filePath, content, true);
	}
	
	public static void recoverFromFile(String filePath){
		BufferedReader br = null;

		Queue<Long> processingQueue = new ConcurrentLinkedQueue<Long>();
		
		List<Long> processedList = new ArrayList<Long>();
		
		if(!new File(filePath).exists()){
			processingQueue.add(SystemConstant.StartPointId);
		}else{
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
				String line = "";
				while((line=br.readLine())!=null){
					String[] lineSegs = line.split(":");
					long head = Long.parseLong(lineSegs[0]);
					if(!processedList.contains(head))
						processedList.add(head);
				}
				br.close();
				br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
				
				while((line=br.readLine())!=null){
					String[] lineSegs = line.split(":");
					if(!"".equals(lineSegs[1])&&lineSegs[1].indexOf(",")>-1){
						String[] list = lineSegs[1].split(",");
						for(String l : list){
							long number = Long.parseLong(l);
							if(!processedList.contains(number)&&!processingQueue.contains(number)){
								processingQueue.add(number);
							}
						}
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
			
			while(!processingQueue.isEmpty()){
				Long head = processingQueue.poll();
				int hints = FriendShipScanner.getRemainingHits();
				log.info("remaining hits reach "+hints+" change accounts");
				if(hints<4){
					FriendShipScanner.changeTwitter(++FriendShipScanner.tokenIndex);
					log.info("change accounts to "+FriendShipScanner.tokenIndex);
				}
				hints =  FriendShipScanner.getRemainingHits();
				IDs friends = FriendShipScanner.getFriends(head);
				IDs followers = FriendShipScanner.getFollowers(head);
//				log.info("has processed "+ (++processCount));
				if(friends==null&&followers==null){
					processData(filePath,head, null,null);
					continue;
				}
				
				
				
				long[] friendsArray = friends.getIDs();
				long[] followerArray = followers.getIDs();
			
				processData(filePath,head, friendsArray,followerArray);
				if(friendsArray!=null&&friendsArray.length>0){
					for(long friend : friendsArray){
						if(!processingQueue.contains(friend)){
							processingQueue.add(friend);
						}
					}
				}
				if(followerArray!=null&&followerArray.length>0){
					for(long follower : followerArray){
						if(!processingQueue.contains(follower)){
							processingQueue.add(follower);
						}
					}
				}
				// add the processed id into processed list
				if(!processedList.contains(head)){
					processedList.add(head);
				}	
			}
		
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("config/log4j.properties");
		getFriendshipMap("/tmp/friendship.txt");
//		getFollowerMap();
	}

}
