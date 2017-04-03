package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.config.MyConfig;
import roart.model.GUISize;
import roart.model.ResultItemBytes;
import roart.model.ResultItem;
import roart.model.StockItem;
import roart.util.Constants;
import roart.util.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryPeriodTopBottom extends GraphCategory {

    List<StockItem>[][] stocklistPeriod;

    private int period;

    public GraphCategoryPeriodTopBottom(MyConfig conf, int i,
            String periodText, List<StockItem>[][] stocklistPeriod) {
        super(conf, periodText);
        period = i;
        this.stocklistPeriod = stocklistPeriod;
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            int days = conf.getTableDays();
            int topbottom = conf.getTopBottom();
            String date0 = null;
            String date1 = null;
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            for (int i = days - 1; i > 0; i--) {
                if (!stocklistPeriod[0][i].isEmpty()) {
                    date0 = dt.format(stocklistPeriod[0][i].get(0).getDate());
                    break;
                }
            }
            if (!stocklistPeriod[0][0].isEmpty()) {
                date1 = dt.format(stocklistPeriod[0][0].get(0).getDate());
            }
            {
                DefaultCategoryDataset dataset = StockUtil.getTopChart(days, topbottom,
                        stocklistPeriod, period);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Top period " + title, "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new2"+ period +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
                    retlist.add(stream);
                }
            }
            {
                DefaultCategoryDataset dataset = StockUtil.getBottomChart(days, topbottom,
                        stocklistPeriod, period);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Bottom period " + title, "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new3"+ period +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
                    retlist.add(stream);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
