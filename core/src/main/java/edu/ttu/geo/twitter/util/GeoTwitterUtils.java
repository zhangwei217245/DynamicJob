package edu.ttu.geo.twitter.util;

import twitter4j.GeoLocation;

/**
 *
 * Created by zhangwei on 3/11/16.
 */
public class GeoTwitterUtils {

    public static final double US_LOWER = 24.15275;

    public static final double US_UPPER = 49.751726;

    public static final double US_LEFT = -125.714216667;

    public static final double US_RIGHT = -65.983272667;
    // 1km 30 Arc-Second in decimal degrees
    public static final double CELL_LEN_1KM = 0.008333;

    public static final double CELL_LEN_500M = CELL_LEN_1KM / 2.0d;

    public static final double US_HORIZONTAL_SPAN = US_RIGHT - US_LEFT;

    public static final double US_VERTICAL_SPAN = US_LOWER - US_UPPER;

    public static final int GRID_HORIZONTAL_SIZE = Long.valueOf(Math
            .round(US_HORIZONTAL_SPAN / CELL_LEN_500M)+1).intValue();

    public static final int GRID_VERTICAL_SIZE = Long.valueOf(Math
            .round(US_VERTICAL_SPAN / CELL_LEN_500M)+1).intValue();


    /**
     * Given geo location of the tweet status, return a tuple containing
     * horizontal index and vertical index
     * @param location
     * @return
     */
    public static Tuple<Integer, Integer> getGridIndex(GeoLocation location) {

        int grid_horz = 0;
        int grid_vert = 0;

        boolean horz_found = false;
        boolean vert_found = false;

        while (grid_horz <= GRID_HORIZONTAL_SIZE) {
            double grid_left_boarder = US_LEFT + (grid_horz) * CELL_LEN_500M;
            double grid_right_boarder = US_LEFT + (grid_horz + 1) * CELL_LEN_500M;
            if (grid_left_boarder <= location.getLongitude()
                    && grid_right_boarder > location.getLongitude()) {
                horz_found = true;
                break;
            }
            grid_horz ++;
        }

        while (grid_vert <= GRID_VERTICAL_SIZE) {
            double grid_lower_boarder = US_LOWER + (grid_vert) * CELL_LEN_500M;
            double grid_upper_boarder = US_LOWER + (grid_vert + 1) * CELL_LEN_500M;
            if (grid_lower_boarder <= location.getLatitude()
                    && grid_upper_boarder > location.getLatitude()) {
                vert_found = true;
                break;
            }
            grid_vert ++;
        }

        return new Tuple<>(horz_found?grid_horz:null, vert_found?grid_vert:null);
    }


}
