package org.uiuc.cigi.crawler;

import org.uiuc.cigi.crawler.action.restfulapi.FriendShipCrawler;
import org.uiuc.cigi.crawler.action.searchapi.SearchCrawler;
import org.uiuc.cigi.crawler.action.streamapi.GeoTweetCrawler;
import org.uiuc.cigi.crawler.action.streamapi.KeywordsTweetCrawler;
import org.uiuc.cigi.crawler.config.Configuration;
import org.uiuc.cigi.crawler.util.SystemConstant;
/**
 * crawler factory to generate the crawler instance
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 04:48:09
 *
 */
public class CrawlerFactory {
	public static Crawler getInstance(APIParameters param){
		if(SystemConstant.STREAM_CONST.equalsIgnoreCase(param.type)){
			if(param.keywords!=null){
				if(param.boundary == null){
					return new KeywordsTweetCrawler(param.keywords);
				}else{
					return new GeoTweetCrawler(param.boundary , param.keywords);
				}
			}else{
				return new GeoTweetCrawler(param.boundary , param.keywords);
			}
		}else if(SystemConstant.SEARCH_CONST.equalsIgnoreCase(param.type)){
			if(param.boundary!=null){
				String keywords = Configuration.getValue("Keywords").replace(",", " ");
				return new SearchCrawler(param.boundary , keywords);
			}
		}else if(SystemConstant.RESTFUL_CONST.equalsIgnoreCase(param.type)){
			return new FriendShipCrawler(Configuration.getValue("FriendShip_File"));
		}
		return null;
	}
}
