package org.litesoft.utils;

import java.io.Closeable;
import java.io.IOException;

public class Closeables {
    public static void dispose(Closeable pCloseable) {
        if (pCloseable != null) {
            try {
                pCloseable.close();
            } catch (IOException e) {
                // Whatever
            }
        }
    }
}
