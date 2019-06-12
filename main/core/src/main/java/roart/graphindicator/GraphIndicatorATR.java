package roart.graphindicator;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibATR;

public class GraphIndicatorATR extends GraphIndicator {

    public GraphIndicatorATR(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isATREnabled();
    }

    @Override
    protected Ta getTa() {
        return new TalibATR();
    }
}

