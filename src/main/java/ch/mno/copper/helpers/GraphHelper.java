package ch.mno.copper.helpers;

import ch.mno.copper.data.StoreValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// c.f. https://stackoverflow.com/questions/26556268/jfree-chart-scatter-plot-date-against-time
public class GraphHelper {

    public static JFreeChart createChart(List<StoreValue> values, String yLabel) {
        XYDataset dataset = createDataset(values);



        JFreeChart chart = ChartFactory.createTimeSeriesChart("Values",
                "Time",                // data
                "°C",                   // include legend
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
        TimeSeries ts = new TimeSeries("Temperature");

        for (StoreValue value: values) {
            ts.add(new Second(Date.from(value.getTimestampFrom())), Float.parseFloat(value.getValue()));
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(ts);
        return dataset;
    }

}
