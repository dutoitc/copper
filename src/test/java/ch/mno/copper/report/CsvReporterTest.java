package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by dutoitc on 26.04.2019.
 */
public class CsvReporterTest {

    @Test
    public void testOne() throws IOException, ConnectorException {
        // New file
        File file = File.createTempFile("copper", "tmp");
        file.delete();
        file.deleteOnExit();

        // Some values
        Map<String, String> values = new HashMap<>();
        values.put(CsvReporter.PARAMETERS.FILENAME.name(), file.getAbsolutePath());
        values.put(CsvReporter.PARAMETERS.HEADERS.name(), "value1;value2;value3");
        values.put(CsvReporter.PARAMETERS.LINE.name(), "aValue;anotherValue;lastValue");
        CsvReporter reporter = new CsvReporter();
        reporter.report(null, values);

        // Test
        String res = IOUtils.toString(new FileInputStream(file));
        assertEquals("value1;value2;value3\r\naValue;anotherValue;lastValue\r\n", res);

        // More values
        values = new HashMap<>();
        values.put(CsvReporter.PARAMETERS.FILENAME.name(), file.getAbsolutePath());
        values.put(CsvReporter.PARAMETERS.HEADERS.name(), "value1;value2;value3");
        values.put(CsvReporter.PARAMETERS.LINE.name(), "123;456;789");
        reporter.report(null, values);

        // Test
        res = IOUtils.toString(new FileInputStream(file));
        assertEquals("value1;value2;value3\r\naValue;anotherValue;lastValue\r\n123;456;789\r\n", res);

    }
}
