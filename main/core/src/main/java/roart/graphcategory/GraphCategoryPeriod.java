package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.graphindicator.GraphIndicatorSTOCHRSI;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryPeriod extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryPeriod(IclijConfig conf, int i, String periodText, 
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(conf, periodText);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        String market = conf.getConfigData().getMarket();
        MarketData marketData = marketdatamap.get(market);
        if (MetaUtil.currentYear(marketData, periodText)) {
            indicators.add(new GraphIndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, title));
            indicators.add(new GraphIndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, title));
        }
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            String periodText = title;
            log.info("check3 {} {}", periodText, periodDataMap.keySet());
            int days = conf.getTableDays();
            int topbottom = conf.getTopBottom();

            PeriodData perioddata = periodDataMap.get(periodText);
            DefaultCategoryDataset dataset = StockUtil.getFilterChartPeriod(days, ids, marketdatamap, perioddata);
            if (dataset != null) {
                JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
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

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
