package org.litesoft.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class IOUtils {
    public static final String UTF_8 = "UTF-8";

    public static BufferedReader createReader(InputStream pInputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(pInputStream, UTF_8));
    }

    public static BufferedWriter createWriter(OutputStream pOS) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(pOS, UTF_8));
    }

    public static String[] loadTextFile(BufferedReader pReader) throws FileSystemException {
        try {
            List<String> lines = new LinkedList<String>();
            boolean closed = false;
            try {
                for (String line; null != (line = pReader.readLine()); ) {
                    lines.add(line);
                }
                closed = true;
                pReader.close();
            } finally {
                if (!closed) {
                    Closeables.dispose(pReader);
                }
            }
            return lines.toArray(new String[lines.size()]);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

}
