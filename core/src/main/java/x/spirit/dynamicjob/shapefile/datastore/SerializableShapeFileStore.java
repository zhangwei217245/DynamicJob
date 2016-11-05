package x.spirit.dynamicjob.shapefile.datastore;

import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by zhangwei on 11/5/16.
 */
public class SerializableShapeFileStore extends ShapefileDataStore implements Serializable {

    private static final long serialVersionUID = 1105635686534305786L;

    public SerializableShapeFileStore(URL url) {
        super(url);
    }
}
