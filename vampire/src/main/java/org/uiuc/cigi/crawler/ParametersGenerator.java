package org.uiuc.cigi.crawler;

import org.uiuc.cigi.crawler.config.Configuration;

/**
 * form parameters from the config file
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 02:42:09
 *
 */
public class ParametersGenerator {
	
	public static APIParameters perform(){
		String type = Configuration.getValue("Type");
		String boundaryStr = Configuration.getValue("BoundingBox");
		double[][]boundary = null;
		
		if(!"".equals(boundaryStr)){
			String[] segs = boundaryStr.split(",");
			boundary = new double[2][2];
			boundary[0][0] = Double.parseDouble(segs[0]);
			boundary[0][1] = Double.parseDouble(segs[1]);
			boundary[1][0] = Double.parseDouble(segs[2]);
			boundary[1][1] = Double.parseDouble(segs[3]);
		}
		
		String[] keywords = null;
		
		String keywordsStr = Configuration.getValue("Keywords");
		if(!"".equals(keywordsStr)){
			keywords = keywordsStr.split(",");
		}
		return new APIParameters(type , boundary, keywords);
	}
}
