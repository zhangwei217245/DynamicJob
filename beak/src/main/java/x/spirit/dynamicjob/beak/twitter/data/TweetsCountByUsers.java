package x.spirit.dynamicjob.beak.twitter.data;


import twitter4j.Status;
import x.spirit.dynamicjob.beak.twitter.util.GeoTwitterUtils;
import x.spirit.dynamicjob.beak.twitter.util.Tuple;

/**
 * Created by zhangwei on 3/11/16.
 */
public class TweetsCountByUsers {

    public static final Integer[][] GEOGRID =
            new Integer[GeoTwitterUtils.GRID_VERTICAL_SIZE][GeoTwitterUtils.GRID_HORIZONTAL_SIZE];

    /**
     * initialize the GEOGRID
     */
    static {
        for (int i = 0; i < GEOGRID.length; i++) {
            for (int j = 0; j < GEOGRID[i].length; j++) {
                GEOGRID[i][j] = new Integer(0);
            }
        }


    }

    public void incrementGridCount(Status status) {
        Tuple<Integer, Integer> gridIndex = GeoTwitterUtils.getGridIndex(status.getGeoLocation());
        if (gridIndex == null) {
            return;
        }
        if (gridIndex.isFullFilled()) {
            if (GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()] != null) {
                System.out.println(GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]);
                synchronized (GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]) {
                    GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]++;
                }
            }
        }
        //printGrid();
    }


    public void printGrid() {
        System.out.println("\n=======================\n");
        for (Integer[] row : GEOGRID) {
            for (Integer cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }
}
