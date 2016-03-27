package x.spirit.dynamicjob.beak.twitter.tiff;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.File;

/**
 *
 * http://www.oracle.com/technetwork/java/jaiutils-138041.html
 * http://www.javalobby.org/articles/ultimate-image/
 * http://www.geotoolkit.org
 * http://sis.apache.org/book/en/developer-guide.html
 * http://sis.apache.org
 * http://geoexamples.blogspot.com/2012/05/running-gdal-java.html
 * @author zhangwei
 */
public class GeoTiffHandler {

    public static void main(String[] args) {
        int rows = 3;
        int cols = 3;

        float[][] imagePixelData = new float[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                float pixel = (float) (index);
//                if (unitySeconds > 0)
//                    pixel /= unitySeconds;
                imagePixelData[row][col] = pixel;
            }
        }
//        GeneralEnvelope generalEnvelope = new GeneralEnvelope(2);
//        generalEnvelope.add(new DirectPosition2D());
        GridCoverage2D coverage2D = new GridCoverageFactory().create("OTPAnalyst", imagePixelData,
                null);
        try {
            GeoTiffWriteParams wp = new GeoTiffWriteParams();
            wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
            wp.setCompressionType("LZW");
            ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
            GeoTiffWriter writer = new GeoTiffWriter(new File("/Users/zhangwei/Downloads/test.tif"));
            writer.write(coverage2D, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
