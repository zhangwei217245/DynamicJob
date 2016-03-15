package edu.ttu.geo.twitter.util;

import twitter4j.GeoLocation;

import java.math.BigDecimal;

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

    public static final double US_VERTICAL_SPAN = US_UPPER - US_LOWER;

    public static final int GRID_HORIZONTAL_SIZE = BigDecimal.valueOf(US_HORIZONTAL_SPAN / CELL_LEN_500M)
                                                    .intValue() + 1;

    public static final int GRID_VERTICAL_SIZE = BigDecimal.valueOf(US_VERTICAL_SPAN / CELL_LEN_500M)
                                                    .intValue() + 1;


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

        if (location.getLongitude() >= US_LEFT && location.getLongitude() <= US_RIGHT) {
            grid_horz = BigDecimal.valueOf((location.getLongitude() - US_LEFT) / CELL_LEN_500M)
                    .intValue() + 1;
            horz_found = true;
        }

        if (location.getLatitude() >= US_LOWER && location.getLatitude() <= US_UPPER) {
            grid_vert = BigDecimal.valueOf((location.getLatitude() - US_LOWER) / CELL_LEN_500M)
                    .intValue() + 1;
            vert_found = true;
        }

        return new Tuple<>(horz_found?grid_horz:null, vert_found?grid_vert:null);
    }


}
