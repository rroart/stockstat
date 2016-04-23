package roart.util;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Image;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class SvgUtil {

    public static JFreeChart getChart(DefaultCategoryDataset dataset, String title, String titleX, String titleY, int xsize, int ysize) {
        JFreeChart lineChart = ChartFactory.createLineChart(
                title,
                titleX, titleY,
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false);
        return lineChart;
    }

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

    public static void bla2(Layout layout, Resource res) {
        //Resource res = new ThemeResource("images/pygame_icon.svg");
    
     // Display the object
        System.out.println("bla " + res.toString());
     Embedded object = new Embedded("My SVG", res);
     object.setMimeType("image/svg+xml"); // Unnecessary
     //object.setHeight(480, Sizeable.Unit.PIXELS );
     //object.setWidth(800, Sizeable.Unit.PIXELS );
     System.out.println("isze" + object.getWidth() + " " + object.getHeight());
     System.out.println("tostr "+object.getSource().getMIMEType());
     layout.addComponent(object);
    }

    public static void bla3(Layout layout, Resource res) {
        BrowserFrame embedded = new BrowserFrame("SVG", res);
        //embedded.setSizeFull();
        //embedded.setHeight(480, Sizeable.Unit.PIXELS );
        //embedded.setWidth(800, Sizeable.Unit.PIXELS );
        //embedded.setHeight("200");
       embedded.setVisible(true);
        System.out.println("isze" + embedded.getWidth() + " " + embedded.getHeight());
       layout.addComponent(embedded);
    }

    public static void bla4(VerticalLayout tab) {
        ExternalResource img = new ExternalResource ("http://vignette2.wikia.nocookie.net/farscape/images/0/04/Moya1.jpg");
        Embedded image = new Embedded("1", img);
        System.out.println("isze" + image.getWidth() + " " + image.getHeight());
       tab.addComponent(image);
    }

    public static void bla5(Layout tab,Resource res) {
        Image image = new Image ("Image", res);
        //Embedded image = new Embedded("1", img);
        image.setHeight(480, Sizeable.Unit.PIXELS );
        image.setWidth(800, Sizeable.Unit.PIXELS );
        System.out.println("isze" + image.getWidth() + " " + image.getHeight());
        tab.addComponent(image);
    }
   
    public static StreamResource chartToResource(JFreeChart c, String name, int xsize, int ysize) {
        StreamResource resource = null;
        try {
            final OutputStream out = SvgUtil.exportChartAsSVG(c, new Rectangle(300 + 10 * xsize, 400 + 10 * ysize), new File(name));
            byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();
            //System.out.println("bytes " + bytes.length + " "+ new String(bytes));
            //System.out.println("size " + (300 + 10 * xsize) + " " + (400 + 10 * ysize));
            resource = new StreamResource(new StreamSource() {
                @Override
                public InputStream getStream() {
                    try {
                        return new ByteArrayInputStream(bytes);
                    } catch (Exception e) {
                        //log.error(Constants.EXCEPTION, e);
                        return null;
                    }
                }
            }, "/tmp/svg3.svg");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resource;
    }

   
}