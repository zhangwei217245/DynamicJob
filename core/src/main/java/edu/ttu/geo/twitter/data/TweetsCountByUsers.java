package edu.ttu.geo.twitter.data;

import edu.ttu.geo.twitter.util.GeoTwitterUtils;
import edu.ttu.geo.twitter.util.Tuple;
import twitter4j.Status;

/**
 * Created by zhangwei on 3/11/16.
 */
public class TweetsCountByUsers {

    public static final Integer[][] GEOGRID =
            new Integer[GeoTwitterUtils.GRID_VERTICAL_SIZE][GeoTwitterUtils.GRID_HORIZONTAL_SIZE];


    public void incrementGridCount(Status status) {
        Tuple<Integer, Integer> gridIndex = GeoTwitterUtils.getGridIndex(status.getGeoLocation());
        if (gridIndex.isFullFilled()) {
            synchronized (GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]) {
                GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]++;
            }
        }
    }


    public void printGrid() {
        for (Integer[] row : GEOGRID) {
            for (Integer cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }
}
