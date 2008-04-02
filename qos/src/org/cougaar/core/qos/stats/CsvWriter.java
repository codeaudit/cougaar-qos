package org.cougaar.core.qos.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.cougaar.util.log.Logger;

public class CsvWriter<T> {
    private final CsvFormat<T> csv;
    private final File file;
    private final Logger log;

    public CsvWriter(CsvFormat<T> csv, String fileName, Logger log) {
        this.csv = csv;
        this.log = log;
        this.file = new File(fileName);
    }

    public void writeRow(T row) {
        boolean oldFile = file.exists();
        try {
            if (oldFile) {
                log.info("Reused csv file " + file);
            } else {
                log.info("Created new csv file " + file);
            }
            FileOutputStream fos = new FileOutputStream(file, oldFile);
            PrintStream out = new PrintStream(fos);
            if (!oldFile) {
                // Only write the labels once.
                csv.writeLabels(out);
            }
            csv.writeRow(row, out);
            fos.close();
        } catch (Exception e) {
            log.error("Error writing to " + file);
        }
    }

    public static <T> void writeRow(T row, CsvFormat<T> format, String filename, Logger log) {
        if (!filename.equals("")) {
            CsvWriter<T> writer = new CsvWriter<T>(format, filename, log);
            writer.writeRow(row);
        }
    }
}
