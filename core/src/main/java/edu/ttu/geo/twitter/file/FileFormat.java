package edu.ttu.geo.twitter.file;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by zhangwei on 3/11/16.
 *
 */
public enum FileFormat {

    TEXT {
        @Override
        public Reader getFileReader(String filePath) throws IOException {
            return new FileReader(filePath);
        }

        @Override
        public Reader getFileReader(InputStream inputStream) throws IOException {
            return new InputStreamReader(inputStream);
        }
    },
    GUNZIP {
        @Override
        public Reader getFileReader(String filePath) throws IOException {
            return new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath)));
        }

        @Override
        public Reader getFileReader(InputStream inputStream) throws IOException {
            return new InputStreamReader(new GZIPInputStream(inputStream));
        }
    };

    public abstract Reader getFileReader(String filePath) throws IOException;
    public abstract Reader getFileReader(InputStream inputStream) throws IOException;
}
