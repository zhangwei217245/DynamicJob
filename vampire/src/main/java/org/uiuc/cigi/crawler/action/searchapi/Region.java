package org.uiuc.cigi.crawler.action.searchapi;
/*
 * this region is thought to be a circle which is well adapt to the search API
 */
public class Region {

	@Override
	public String toString() {
		return "Region [latitude=" + latitude + ", longitude=" + longitude
				+ ", distance=" + distance + ", units=" + units + "]";
	}

	private double latitude;
	
	private double longitude;
	
	private double distance;
	
	private String units;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public Region(double latitude, double longitude, double distance,
			String units) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;
		this.units = units;
	}
	
	public Region(){
		
	}
	
}
