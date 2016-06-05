package org.uiuc.cigi.crawler.action.searchapi;

import org.apache.log4j.Logger;
import org.uiuc.cigi.crawler.Crawler;
import org.uiuc.cigi.crawler.config.Configuration;
import org.uiuc.cigi.crawler.util.BoundingBox;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * search api crawler
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 3:36:21
 *
 */
public class SearchCrawler implements Crawler{
	//static long sinceId = 269008918259310592L;
	static long sinceId =   270381581800198144L;
	static Twitter twitter = new TwitterFactory().getInstance();
	
	public Region region; // specify the concerning region where to get data
	
	public SearchScanner sc;
	
	public List<Region> subRegions = new ArrayList<Region>();
	
	Logger log = Logger.getLogger(this.getClass());
	
	public SearchCrawler(BoundingBox box , String keywords){
		region = box.getRegion();
		sc = new SearchScanner(twitter , sinceId ,keywords);
	}
	
	public SearchCrawler(double[][] box, String keywords){
		region = new BoundingBox(box[0][0],box[0][1],box[1][0],box[1][1]).getRegion();
		sc = new SearchScanner(twitter , sinceId , keywords);
	}
	
	/**
	 * search the whole region and returns the sinceId for next run
	 * @return
	 */
	public long searchRegion(){
		log.info("totally we have "+ subRegions.size()+" subregions");
		long res = 0;
		for(Region sub : subRegions){
			long r = searchSubRegion(sub);
			if(res<r){
				res = r;
			}
		}
		sinceId = res;
		log.info("totally we have "+ subRegions.size()+" subregions and got the sinceId = "+sinceId);
		return res;
	}
	
	/**
	 * search a whole sub region
	 * @param sub
	 */
	public long searchSubRegion(Region sub){
		long res = 0;
		for(int i=1;i<=15;i++){
			sc.setQuery(sub, i);
			TempSearchResult r = sc.search();
			boolean flag = r.flag;
			if(res<r.maxId){
				res = r.maxId;
			}
			if(!flag)
				break;
		}
		return res;
	}
	
	// iteratively divide the region into subregions
	public void divide(Region _region){
		sc.setQuery(_region, 15);
		int amounts = sc.getResultsAmounts();
		if(amounts>=100){
			Region[] regions = RegionUtils.quarterRegion(_region);
			log.info(_region.toString()+" has been quartered");
			for(Region r : regions){
				divide(r);
			}
		}else{
			subRegions.add(_region);
		}
	}

	public void collect() {
		while(true){
			divide(region);
			searchRegion();
			try {
				Thread.sleep(Long.parseLong(Configuration.getValue("Interval_Time"))*60*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void collect(int buffersize) {
		while(true){
			divide(region);
			searchRegion();
			try {
				Thread.sleep(Long.parseLong(Configuration.getValue("Interval_Time"))*60*100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}	
