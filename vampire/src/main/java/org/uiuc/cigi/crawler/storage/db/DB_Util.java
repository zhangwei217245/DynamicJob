package org.uiuc.cigi.crawler.storage.db;

import org.uiuc.cigi.crawler.config.Configuration;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class DB_Util {
	//private static DB_Util instance;
    private static Mongo mongo;
	private DB_Util(){
		
	}

	public static void removePool() {
		
	}

	public synchronized final static DB getConnection() {
		 DB db = null;
	        try {
	        	if(mongo==null){
	        		String dbUrl = Configuration.getValue("DBUrl");
//	        		String dbPort = Configuration.getValue("DBPort");
	        		mongo = new Mongo(dbUrl);
	        		String dbName = Configuration.getValue("DBName");
		            db = mongo.getDB(dbName); 
		            // do the authentication
		            db.authenticate(Configuration.getValue("DBUser"), Configuration.getValue("DBPassword").toCharArray());
	        	}else{
	        		String dbName = Configuration.getValue("DBName");
		            db = mongo.getDB(dbName); 
	        	}
	        	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return db;
	}



}
