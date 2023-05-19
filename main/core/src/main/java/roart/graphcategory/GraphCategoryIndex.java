package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorATR;
import roart.graphindicator.GraphIndicatorCCI;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.graphindicator.GraphIndicatorSTOCH;
import roart.graphindicator.GraphIndicatorSTOCHRSI;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryIndex extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryIndex(IclijConfig conf, String string,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap,title));
        indicators.add(new GraphIndicatorSTOCHRSI(conf, title + " SRSI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorSTOCH(conf, title + " STOCH", marketdatamap, periodDataMap,title));
        indicators.add(new GraphIndicatorATR(conf, title + " ATR", marketdatamap, periodDataMap,title));
        indicators.add(new GraphIndicatorCCI(conf, title + " CCI", marketdatamap, periodDataMap,title));
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            if (StockUtil.hasSpecial(marketdatamap, Constants.INDEXVALUECOLUMN)) {
                String periodText = title;
                int days = conf.getTableDays();
                int topbottom = conf.getTopBottom();

                PeriodData perioddata = periodDataMap.get(periodText);
                DefaultCategoryDataset dataseteq = null;
                if (conf.isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.INDEXVALUECOLUMN, conf.isGraphEqualize(), dataseteq);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, Constants.INDEX, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
                    retlist.add(stream);
                }
                if (dataset != null && dataseteq != null) {
                    JFreeChart c = SvgUtil.getChart(dataseteq, Constants.INDEX, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
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

