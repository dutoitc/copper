package ch.mno.copper.helpers;

import ch.mno.copper.data.StoreValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;

// c.f. https://stackoverflow.com/questions/26556268/jfree-chart-scatter-plot-date-against-time
public class GraphHelper {

    public static Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    public static JFreeChart createChart(List<StoreValue> values, String title, String yLabel) {
        XYDataset dataset = createDataset(values);



        JFreeChart chart = ChartFactory.createTimeSeriesChart("Values",
                "Time",                // data
                yLabel,                   // include legend
                dataset,
                true,
                true,
                false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);

        plot.setRangeGridlinePaint(Color.WHITE);
//        plot.getRangeAxis().setRange(10, 50);                       //graph displays 10-50

        return chart;
    }

    private static XYDataset createDataset(List<StoreValue> values) {
        TimeSeries ts = new TimeSeries("");

        for (StoreValue value: values) {
            try {
                ts.add(new Second(Date.from(value.getTimestampFrom())), Float.parseFloat(value.getValue()));
            } catch (NumberFormatException e) {
                LOG.warn("Skipping invalid value '" + value.getValue() + "' for key '" + value.getKey() + "'");
            }
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(ts);
        return dataset;
    }

    public static byte[] toPNG(JFreeChart chart, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
        Rectangle r = new Rectangle(0, 0, width, height);
        chart.draw(g2, r);
        BufferedImage chartImage = chart.createBufferedImage(width, height, null);
        return ChartUtilities.encodeAsPNG(chartImage);
    }
}
