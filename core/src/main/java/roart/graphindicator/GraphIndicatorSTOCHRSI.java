package roart.graphindicator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.config.MyConfig;
import roart.model.GUISize;
import roart.model.ResultItemBytes;
import roart.model.ResultItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.SvgUtil;
import roart.util.TaUtil;

public class GraphIndicatorSTOCHRSI extends GraphIndicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    String key;

    public GraphIndicatorSTOCHRSI(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
    }

    @Override
    public boolean isEnabled() {
        return conf.isSTOCHRSIEnabled();
    }

    @Override
    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            String periodText = key;
            int days = conf.getTableDays();
            int topbottom = conf.getTopBottom();
            PeriodData perioddata = periodDataMap.get(periodText);
            TaUtil tu = new TaUtil();
            for (Pair id : ids) {
                String market = (String) id.getFirst();
                String stockid = (String) id.getSecond();
                DefaultCategoryDataset dataset = tu.getSTOCHRSIChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+".svg", days, topbottom, conf.getTableDays(), 1, guiSize, Constants.INDICATORSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = false;
                    retlist.add(stream);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

