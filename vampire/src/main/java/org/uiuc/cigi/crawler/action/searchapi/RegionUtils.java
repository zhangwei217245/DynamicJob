package org.uiuc.cigi.crawler.action.searchapi;

public class RegionUtils {
	
	final static double DISTANCE_PER_DEGREE = 111;// UNITS  KM
	
	/*
	 * divide the big region into four sub regions
	 */
	public static Region[] quarterRegion(Region region){
		
		double dis = 0;
		if("km".equalsIgnoreCase(region.getUnits())){
			dis = region.getDistance();
		}else if("mi".equalsIgnoreCase(region.getUnits())){
			dis = region.getDistance()*1.609344;
		}
		
		double xPan = dis/Math.sqrt(2);
		double deltaX = xPan/DISTANCE_PER_DEGREE;
		double deltaY = xPan/DISTANCE_PER_DEGREE;
		
		Region[] _regions = new Region[4];
		_regions[0] = new Region();
		_regions[0].setUnits(region.getUnits());
		_regions[0].setDistance(region.getDistance()/2);
		_regions[0].setLatitude(region.getLatitude()+deltaX/2);
		_regions[0].setLongitude(region.getLongitude()-deltaY/2);
		
		_regions[1] = new Region();
		_regions[1].setUnits(region.getUnits());
		_regions[1].setDistance(region.getDistance()/2);
		_regions[1].setLatitude(region.getLatitude()+deltaX/2);
		_regions[1].setLongitude(region.getLongitude()+deltaY/2);
		
		_regions[2] = new Region();
		_regions[2].setUnits(region.getUnits());
		_regions[2].setDistance(region.getDistance()/2);
		_regions[2].setLatitude(region.getLatitude()-deltaX/2);
		_regions[2].setLongitude(region.getLongitude()+deltaY/2);
		
		_regions[3] = new Region();
		_regions[3].setUnits(region.getUnits());
		_regions[3].setDistance(region.getDistance()/2);
		_regions[3].setLatitude(region.getLatitude()-deltaX/2);
		_regions[3].setLongitude(region.getLongitude()-deltaY/2);
		
		return _regions;
	}
}
