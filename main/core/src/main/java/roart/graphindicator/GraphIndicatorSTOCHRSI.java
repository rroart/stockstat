package roart.graphindicator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.util.SvgUtil;
import roart.talib.Ta;
import roart.talib.impl.Ta4jSTOCHRSI;
import roart.talib.impl.TalibSTOCHRSI;

public class GraphIndicatorSTOCHRSI extends GraphIndicator {

    public GraphIndicatorSTOCHRSI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isSTOCHRSIEnabled();
    }

    @Override
    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        super.getResult(retlist, ids, guiSize);
        super.getResult(retlist, ids, guiSize, new Ta4jSTOCHRSI());
    }

    @Override
    protected Ta getTa() {
        return new TalibSTOCHRSI();
    }

}

