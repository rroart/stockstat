package roart.util;

import java.io.FileNotFoundException;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.Test;

public class SvgUtilTest {

    @Test
    public void testGet() throws Exception {
        int days = 0;
        int topbottom = 0;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Integer value = 42;
        Integer order = 1;
        dataset.addValue(value, "type 1", order);
        //JFreeChart c = SvgUtil.getChart(dataset, "Index", "Time " + "date 1" + " - " + "date 2", "Value", days, topbottom);
        //assertEquals(c.getSubtitleCount(), 1);
    }

}
