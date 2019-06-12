package roart.graphindicator;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibSTOCH;

public class GraphIndicatorSTOCH extends GraphIndicator {

    public GraphIndicatorSTOCH(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isSTOCHEnabled();
    }

    @Override
    protected Ta getTa() {
        return new TalibSTOCH();
    }

}

