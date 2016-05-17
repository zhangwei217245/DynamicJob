package org.uiuc.cigi.crawler.util;

import org.uiuc.cigi.crawler.config.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
/**
 * read tokens from text file including twitter developer authentication file
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 1:23:08
 *
 */
public class TokenReader {

	public static ArrayList<ConfigurationBuilder> readData(){
		ArrayList<ConfigurationBuilder> cbList = new ArrayList<ConfigurationBuilder>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(Configuration.defaultPath()+"twitterToken.txt")));
			String line = "";
			int lineLimit = 0;
			ConfigurationBuilder cb = null;
			while((line=br.readLine())!=null){
				if(line.startsWith("#")){
					lineLimit = 0;
					cb = new ConfigurationBuilder();
					cb.setDebugEnabled(true);
					cb.setStreamBaseURL(SystemConstant.STREAMING_BASE_URL);
				    //cb.setUseSSL(true);
				}else{
					lineLimit++;
					switch(lineLimit){
					case 1: 
						cb.setOAuthConsumerKey(line);
						break;
					case 2:
						cb.setOAuthConsumerSecret(line);
						break;
					case 3: 
						cb.setOAuthAccessToken(line);
						break;
					case 4: 
						cb.setOAuthAccessTokenSecret(line);
						cbList.add(cb);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cbList;
	}
	public static void main(String[] args) {
		readData();

	}

}
