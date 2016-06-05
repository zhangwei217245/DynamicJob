package org.uiuc.cigi.crawler;

/**
 * parameters which would be passed to the api
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 02:36:13
 *
 */
public class APIParameters {
	
	public String type;
	public double[][] boundary;
	public String[] keywords;
	public APIParameters(String type, double[][] boundary, String[] keywords) {
		super();
		this.type = type;
		this.boundary = boundary;
		this.keywords = keywords;
	}
	
}
