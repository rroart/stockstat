package roart.graphindicator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import roart.iclij.config.IclijConfig;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.talib.Ta;
import roart.talib.impl.Ta4jATR;
import roart.talib.impl.Ta4jSTOCHRSI;
import roart.talib.impl.TalibATR;

public class GraphIndicatorATR extends GraphIndicator {

    public GraphIndicatorATR(IclijConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isATREnabled();
    }

    @Override
    public void getResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        super.getResult(retlist, ids, guiSize);
        super.getResult(retlist, ids, guiSize, new Ta4jATR());
    }

    @Override
    protected Ta getTa() {
        return new TalibATR();
    }
}

