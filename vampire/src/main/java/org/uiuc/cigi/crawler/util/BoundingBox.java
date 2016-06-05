package org.uiuc.cigi.crawler.util;


import org.uiuc.cigi.crawler.action.searchapi.Region;

public class BoundingBox {
	private int id;
	private double westest;
	private double southest;
	private double eastest;
	private double northest;
	private Point centric;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public void setCentricPoint(){
		centric = new Point(westest/2+eastest/2, southest/2+northest/2);
	}
	public Point getCentric() {
		return new Point(westest/2+eastest/2, southest/2+northest/2);
	}
	public void setCentric(Point centric) {
		this.centric = centric;
	}
	public BoundingBox(){
		
	}
	public double getEastest() {
		return eastest;
	}
	public void setEastest(double eastest) {
		this.eastest = eastest;
	}
	public double getWestest() {
		return westest;
	}
	public void setWestest(double westest) {
		this.westest = westest;
	}
	public double getSouthest() {
		return southest;
	}
	public void setSouthest(double southest) {
		this.southest = southest;
	}
	public double getNorthest() {
		return northest;
	}
	public void setNorthest(double northest) {
		this.northest = northest;
	}
	public BoundingBox(double westest, double southest, double eastest,
			double northest) {
		super();
		this.westest = westest;
		this.southest = southest;
		this.eastest = eastest;
		this.northest = northest;
		this.setCentricPoint();
	}
	//whether containing a point
	public boolean isContained(double lng, double lat){
		return lng>=westest&&lng<eastest&&lat>=southest&&lat<northest;
	}
	@Override
	public String toString() {
		return "BoundingBox [ id=" + id + ", ("+westest+","+southest+")("+eastest+","+northest+")]";
	}
	
	// transfer bounding box to centroid+radius, it is an assumption
	public Region getRegion(){
		Region region = new Region();
		double lat = this.centric.getY();
		double lon = this.centric.getX();
		region.setLatitude(lat);
		region.setLongitude(lon);
		
		double radius = Math.sqrt((northest-lat)*(northest-lat)+(eastest-lon)*(eastest-lon))*111;
		String units = "km";
		region.setUnits(units);
		region.setDistance(radius);
		return region;
	}
	
	
}
