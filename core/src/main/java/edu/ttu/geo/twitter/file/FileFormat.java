package edu.ttu.geo.twitter.file;

import java.io.*;
import java.nio.file.Path;
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

        @Override
        public Reader getFileReader(File file) throws IOException {
            return new FileReader(file);
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

        @Override
        public Reader getFileReader(File file) throws IOException {
            return new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
        }
    };

    public abstract Reader getFileReader(String filePath) throws IOException;
    public abstract Reader getFileReader(InputStream inputStream) throws IOException;
    public abstract Reader getFileReader(File file) throws IOException;


    public static Reader getReaderBySuffix(Path filePath) throws IOException{
        if (filePath.endsWith(".txt")) {
            return TEXT.getFileReader(filePath.toFile());
        } else if (filePath.endsWith(".gz")) {
            return GUNZIP.getFileReader(filePath.toFile());
        }
        return null;
    }
}
