package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.config.MyConfig;
import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.graphindicator.GraphIndicatorSTOCHRSI;
import roart.model.GUISize;
import roart.model.ResultItemBytes;
import roart.model.ResultItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryPrice extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryPrice(MyConfig conf, String string,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorSTOCHRSI(conf, title + " SRSI", marketdatamap, periodDataMap, title));
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            if (StockUtil.hasSpecial(marketdatamap, Constants.PRICECOLUMN)) {
                String periodText = title;
                int days = conf.getTableDays();
                int topbottom = conf.getTopBottom();

                System.out.println("check " + periodText + " " + periodDataMap.keySet());
                PeriodData perioddata = periodDataMap.get(periodText);
                //PeriodData perioddata = new PeriodData();
                DefaultCategoryDataset dataseteq = null;
                if (conf.isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.PRICECOLUMN, conf.isGraphEqualize(), dataseteq);
                if (dataset != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataset, Constants.PRICE, "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    retlist.add(stream);
                }
                if (dataset != null && dataseteq != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataseteq, Constants.PRICE, "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    retlist.add(stream);
                }
                for (GraphIndicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        indicator.getResult(retlist, ids, guiSize);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

