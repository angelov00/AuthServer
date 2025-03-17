package com.angelov00.server.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger implements AutoCloseable {

    private final String filePath;
    private final BufferedWriter bufferedWriter;

    public Logger(String filePath) throws IOException {
        this.filePath = filePath;
        this.bufferedWriter = new BufferedWriter(new FileWriter(filePath, true));
    }

    public synchronized void log(String message) throws IOException {

        this.bufferedWriter.write(message);
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    public void close() throws IOException {
        this.bufferedWriter.close();
    }
}
