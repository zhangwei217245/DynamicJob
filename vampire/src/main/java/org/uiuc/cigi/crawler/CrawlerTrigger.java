package org.uiuc.cigi.crawler;

import org.apache.log4j.PropertyConfigurator;
import org.uiuc.cigi.crawler.config.Configuration;

/**
 * its the main class to run the crawler
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 03:13:20
 *
 */
public class CrawlerTrigger {
	
	public static void run(String[] args){
		APIParameters params = ParametersGenerator.perform();
		
		if (args.length > 0){
                        String keywords = args[0];
			params.keywords = keywords.split(",");
		}
		Crawler crawler = CrawlerFactory.getInstance(params);
	         
                if (args.length == 2){	
			crawler.collect(Integer.parseInt(args[1]));
                }
                else {
			crawler.collect();
                }
	}
	public static void main(String[] args){
		// set the log environment
		PropertyConfigurator.configure(Configuration.defaultPath()+"log4j.properties");
		
		run(args);
	}
}
