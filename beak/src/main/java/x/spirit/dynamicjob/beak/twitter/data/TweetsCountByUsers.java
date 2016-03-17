package x.spirit.dynamicjob.beak.twitter.data;


import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import twitter4j.GeoLocation;
import x.spirit.dynamicjob.beak.twitter.util.GeoTwitterUtils;
import x.spirit.dynamicjob.beak.twitter.util.Tuple;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangwei on 3/11/16.
 */
public class TweetsCountByUsers {

    public static final Map<Long, AtomicInteger>[][] GEOGRID =
            new Map[GeoTwitterUtils.GRID_VERTICAL_SIZE][GeoTwitterUtils.GRID_HORIZONTAL_SIZE];

    /**
     * initialize the GEOGRID
     */
    static {
        for (int i = 0; i < GEOGRID.length; i++) {
            for (int j = 0; j < GEOGRID[i].length; j++) {
                GEOGRID[i][j] = new ConcurrentHashMap<>();
            }
        }


    }

    public void incrementGridCount(String statusJson) {
        try {
            ReadContext ctx = JsonPath.parse(statusJson, Configuration.builder()
                    .jsonProvider(new JacksonJsonProvider())
                    .mappingProvider(new JacksonMappingProvider())
                    .options(Option.SUPPRESS_EXCEPTIONS,
                            Option.DEFAULT_PATH_LEAF_TO_NULL).build()
            );
            Tuple<Integer, Integer> gridIndex = null;
            if (ctx.read("$.coordinates") != null) {
                Double longitude = ctx.read("$.coordinates.coordinates[0]", Double.class);
                Double latitude = ctx.read("$.coordinates.coordinates[1]", Double.class);
                System.out.println(String.format("longitude %s latitude %s", longitude, latitude));
                GeoLocation geoLocation = new GeoLocation(latitude, longitude);
                gridIndex = GeoTwitterUtils.getGridIndex(geoLocation);
            }

            if (gridIndex == null) {
                return;
            }
            if (gridIndex.isFullFilled()) {
                if (GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()] != null) {
                    //System.out.println(GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()]);
                    Map<Long, AtomicInteger> map = GEOGRID[gridIndex.getSecond()][gridIndex.getFirst()];
                    Long userId = ctx.read("$.user.id", Long.class);
                    AtomicInteger atomicInteger = map.putIfAbsent(userId, new AtomicInteger(1));
                    if (atomicInteger != null) {
                        atomicInteger.incrementAndGet();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //printGrid();
    }


    public static void printGrid(PrintStream out) {
        //System.out.println("\n=======================\n");
        for (Map<Long, AtomicInteger>[] row : GEOGRID) {
            for (Map<Long, AtomicInteger> cell : row) {
                out.print(cell.size() + ",");
            }
            out.println();
        }
    }
}
