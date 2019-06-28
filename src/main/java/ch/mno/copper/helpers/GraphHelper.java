package ch.mno.copper.helpers;

import ch.mno.copper.data.StoreValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.util.Date;
import java.util.List;

// c.f. https://stackoverflow.com/questions/26556268/jfree-chart-scatter-plot-date-against-time
public class GraphHelper {

    public static JFreeChart createChart(List<StoreValue> values, String yLabel) {
        XYDataset dataset = createDataset(values);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Values", "Date", yLabel, dataset);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        DateAxis xAxis = new DateAxis("Date");
        xAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(new DateAxis(yLabel));
        return chart;
    }

    private static XYDataset createDataset(List<StoreValue> values) {
        XYSeries s1 = new XYSeries("S1");
        for (StoreValue value: values) {
            s1.add(new Day(Date.from(value.getTimestampFrom())).getMiddleMillisecond(), Float.parseFloat(value.getValue()));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        return dataset;
    }

}
