package ch.mno.copper.helpers;

import ch.mno.copper.store.StoreValue;
import org.jfree.chart.JFreeChart;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphHelperTest {

    // Simple test which assert no crash and simple png length check
    @Test
    void testAll() throws IOException {
        List<StoreValue> values = new ArrayList<>();
        values.add(new StoreValue(1l, "key", "10", Instant.ofEpochSecond(1000000), null, null, 1));
        JFreeChart chart = GraphHelper.createChart(values, "aTitle", "aLabel");
        assertEquals("aTitle", chart.getTitle().getText());

        byte[] png = GraphHelper.toPNG(chart, 10, 10);
        assertTrue(png.length>100, "Wrong size: "+png.length);
    }

}
