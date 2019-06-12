package roart.graphindicator;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibCCI;

public class GraphIndicatorCCI extends GraphIndicator {

    public GraphIndicatorCCI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isCCIEnabled();
    }

    @Override
    protected Ta getTa() {
        return new TalibCCI();
    }

}

