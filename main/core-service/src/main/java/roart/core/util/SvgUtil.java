package roart.core.util;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import roart.common.constants.Constants;
import roart.result.model.GUISize;

public class SvgUtil {

    private static Logger log = LoggerFactory.getLogger(SvgUtil.class);

    /**
     * Make a chart out of the dataset
     * 
     * @param dataset Dataset
     * @param title Main title
     * @param titleX X axis title
     * @param titleY Y axis title
     * @param xsize deprecated
     * @param ysize deprecated
     * @return
     */

    public static JFreeChart getChart(DefaultCategoryDataset dataset, String title, String titleX, String titleY, int xsize, int ysize) {
        return ChartFactory.createLineChart(
                title,
                titleX, titleY,
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false);
    }

    /**
     * Create an SVG image stream from a chart
     * 
     * @param chart The input chart
     * @param bounds The suggested 2D size
     * @param svgFile The written debug file
     * @return an output stream to the SVG image
     * @throws IOException
     */

    public static OutputStream exportChartAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);                       
        outputStream.flush();
        outputStream.close();
        outputStream = new ByteArrayOutputStream();
        out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);
        svgGenerator.stream(out, true /* use css */);                       
        outputStream.flush();
        outputStream.close();
        return outputStream;
    }

    /**
     * Create a stream resource from the chart, and write a debug image file
     * 
     * @param chart Chart
     * @param name Filename for the debug image
     * @param xsize2 Suggested X axis size
     * @param ysize2 Suggested Y axis size
     * @param days days
     * @param days how many days to display
     * @param topbottom top count
     * @param topbottom how many items
     * @param guiSize gui size
     * @param size size
     * @return a stream resource for the image
     */

    public static OutputStream chartToStream(JFreeChart chart, String name, int xsize2, int ysize2, int days, int topbottom, GUISize guiSize, int size) {
        try {
            int xsize = 0*200 + 1*100 + 300 + 10 * days;
            int ysize = 0*200 + 1*200 + 400 + 10 * topbottom;
            if (xsize + 0*100 > guiSize.x) {
                xsize = guiSize.x - 0*100 - 200;
            }
            // gui size is wrong, temp fix
            xsize = 1024;
            ysize = 768;
            ysize = size * ysize / 100;
            return SvgUtil.exportChartAsSVG(chart, new Rectangle(xsize, ysize), new File(name));
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }


}
