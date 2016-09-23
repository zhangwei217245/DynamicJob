package x.spirit.dynamicjob.core.utils;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangwei on 9/1/16.
 */
public class ShapeFileUtils {


    public static List<Object> getAttribute(ShapefileDataStore dataStore, double x, double y, String typeName,
                                            String[] names) throws IOException, CQLException {
        Filter gisFilter = CQL.toFilter(String.format("CONTAINS(the_geom, POINT(%1$.10f %2$.10f))", x, y));
        return getAttribute(dataStore, gisFilter, typeName, names);
    }

    public static List<Object> getAttribute(ShapefileDataStore dataStore, Filter gisFilter, String typeName,
                                            String[] names) throws IOException {
        List<Object> result = new ArrayList();
        SimpleFeatureSource sfs = dataStore.getFeatureSource();
        SimpleFeatureCollection featureCollection = sfs.getFeatures(gisFilter);
        SimpleFeatureIterator simpleFeatureIterator = featureCollection.features();
        while (simpleFeatureIterator.hasNext()) {
            SimpleFeature simpleFeature = simpleFeatureIterator.next();
            if (simpleFeature.getFeatureType().getTypeName().equals(typeName)){
                for (String name : names) {
                    result.add(simpleFeature.getAttribute(name));
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            ShapefileDataStore dataStore = new ShapefileDataStore(
                    new File("/Users/zhangwei/Downloads/County_2010Census_DP1/County_2010Census_DP1.shp").toURI().toURL());

            System.out.println(String.format("CONTAINS(the_geom, POINT(%1$.10f %2$.10f))", -84.14617687, 33.72176483));

            Filter gisFilter = CQL.toFilter(String.format("CONTAINS(the_geom, POINT(%1$.10f %2$.10f))", -84.14617687, 33.72176483));

            String[] names = new String[20];
            for (int n = 1; n <= 20; n++) {
                int base = 80000 + n;
                names[n-1] = "DP00" + base;
            }
            for (Object o : getAttribute(dataStore, gisFilter, "County_2010Census_DP1", names)) {
                System.out.println(o.getClass());
            }
//            while (true) {
//                SimpleFeatureSource sfs = dataStore.getFeatureSource();
//                SimpleFeatureCollection featureCollection = sfs.getFeatures(gisFilter);
//                SimpleFeatureIterator simpleFeatureIterator = featureCollection.features();
//                long start = System.currentTimeMillis();
//                while (simpleFeatureIterator.hasNext()) {
//                    SimpleFeature simpleFeature = simpleFeatureIterator.next();
//                    System.out.println("=============" + simpleFeature.getBounds() + " : " +
//                            simpleFeature.getFeatureType().getTypeName() + "=============");
//                    for (int i = 0; i < simpleFeature.getAttributeCount(); i++) {
//                        System.out.println(simpleFeature.getName() + " : " + simpleFeature.getAttribute(i));
//                    }
//                    System.out.println("=======================================");
//                }
//
//                System.out.println(System.currentTimeMillis() - start);
//                Thread.sleep(10);
//            }

//            ShapefileReader r = new ShapefileReader(
//                    new ShpFiles("/Users/zhangwei/Downloads/County_2010Census_DP1/County_2010Census_DP1.shp"),
//            false, false, gf);
//            int index = 0;
//            while (r.hasNext()) {
//                index++;
//                ShapefileReader.Record rc = r.nextRecord();
//                Geometry shape = (Geometry) rc.shape();
//                if (shape.covers(gf.createPoint(new Coordinate(-84.14617687,33.72176483)))){
//                    System.out.println(index);
//                }
//
//                // do stuff
//            }
//            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CQLException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
    }
}
