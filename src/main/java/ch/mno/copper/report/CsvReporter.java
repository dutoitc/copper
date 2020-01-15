package ch.mno.copper.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class CsvReporter implements AbstractReporter  {

    public enum PARAMETERS {FILENAME, HEADERS, LINE};


    @Override
    public void report(String message, Map<String, String> values) {
        String filename = values.get(CsvReporter.PARAMETERS.FILENAME.toString());
        String header = values.get(CsvReporter.PARAMETERS.HEADERS.toString());
        String line = values.get(CsvReporter.PARAMETERS.LINE.toString());

        // TODO: add file lock while writing + wait ?
        File file = new File(filename);
        if (file.exists()) {
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.append(line).append("\r\n");
                fw.flush();
                fw.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileWriter fw = new FileWriter(file)) {
                fw.append(header).append("\r\n");
                fw.append(line).append("\r\n");
                fw.flush();
                fw.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
