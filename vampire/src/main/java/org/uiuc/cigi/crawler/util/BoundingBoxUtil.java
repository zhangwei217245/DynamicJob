package org.uiuc.cigi.crawler.util;


public class BoundingBoxUtil {
	
	// get the resulting boxes for continental United States
	public static BoundingBox[][] cutIntoRasters(int xSize, int ySize){
		BoundingBox[][] boxes = new BoundingBox[xSize][ySize];
		BoundingBox usaBox =null;
		double xLength = usaBox.getEastest()-usaBox.getWestest();
		double yLength = usaBox.getNorthest()-usaBox.getSouthest();
		double xUnit = xLength/xSize;
		double yUnit = yLength/ySize;
		for(int i=0;i<xSize;i++){
			for(int j=0;j<ySize;j++){
				BoundingBox box = new BoundingBox(usaBox.getWestest()+i*xUnit,usaBox.getSouthest()+j*yUnit,usaBox.getWestest()+(i+1)*xUnit,usaBox.getSouthest()+(j+1)*yUnit);
			    box.setCentricPoint();
				boxes[i][j] = box;
			}
		}
		return boxes;
	}
	
	public static BoundingBox[][] cutIntoRasters(BoundingBox totalBox,int xSize, int ySize){
		BoundingBox[][] boxes = new BoundingBox[xSize][ySize];
		double xLength = totalBox.getEastest()-totalBox.getWestest();
		double yLength = totalBox.getNorthest()-totalBox.getSouthest();
		double xUnit = xLength/xSize;
		double yUnit = yLength/ySize;
		for(int i=0;i<xSize;i++){
			for(int j=0;j<ySize;j++){
				BoundingBox box = new BoundingBox(totalBox.getWestest()+i*xUnit,totalBox.getSouthest()+j*yUnit,totalBox.getWestest()+(i+1)*xUnit,totalBox.getSouthest()+(j+1)*yUnit);
			    box.setCentricPoint();
			    box.setId(i*ySize+j);
				boxes[i][j] = box;
			}
		}
		return boxes;
	}
	//reduce the array dimension
	public static BoundingBox[] reduceDimension(BoundingBox[][] boxes){
		BoundingBox[] boxesRes = new BoundingBox[boxes.length*boxes[0].length];
		for(int i=0;i<boxesRes.length;i++){
			boxesRes[i] = boxes[i/boxes.length][i%boxes.length];
		}
		return boxesRes;
	}
	
	//
	public static void doFlowMap(){
		
	}
	
}
