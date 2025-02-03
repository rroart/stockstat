package roart.core.graphindicator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.talib.Ta;
import roart.core.util.SvgUtil;

public abstract class GraphIndicator {

    protected static Logger log = LoggerFactory.getLogger(GraphIndicator.class);

    protected String title;
    protected IclijConfig conf;
    protected Map<String, MarketData> marketdatamap;
    protected Map<String, PeriodData> periodDataMap;
    protected String key;

    public GraphIndicator(IclijConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        this.title = string;
        this.conf = conf;
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
    }

    public abstract boolean isEnabled();

    public String getResultItemTitle() {
        return title;
    }

    protected abstract Ta getTa();

    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        Ta ta = getTa();
        getResult(retlist, ids, guiSize, ta);
    }
    
    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize, Ta ta) {
        try {
            String periodText = key;
            int days = conf.getTableDays();
            int topbottom = conf.getTopBottom();
            PeriodData perioddata = periodDataMap.get(periodText);
            for (Pair<String, String> id : ids) {
                String market = id.getLeft();
                String stockid = id.getRight();
                DefaultCategoryDataset dataset = ta.getChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
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

