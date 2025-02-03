package roart.core.graphindicator;

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
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.core.util.SvgUtil;
import roart.talib.Ta;
import roart.talib.impl.Ta4jMACD;
import roart.talib.impl.Ta4jSTOCHRSI;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaUtil;

public class GraphIndicatorMACD extends GraphIndicator {

    public GraphIndicatorMACD(IclijConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isMACDEnabled();
    }

    @Override
    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        super.getResult(retlist, ids, guiSize);
        super.getResult(retlist, ids, guiSize, new Ta4jMACD());
    }

    @Override
    protected Ta getTa() {
        return new TalibMACD();
    }
}

