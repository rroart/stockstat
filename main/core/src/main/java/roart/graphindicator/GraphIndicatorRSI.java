package roart.graphindicator;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibRSI;

public class GraphIndicatorRSI extends GraphIndicator {

    public GraphIndicatorRSI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title) {
        super(conf, string, marketdatamap, periodDataMap, title);
    }

    @Override
    public boolean isEnabled() {
        return conf.isRSIEnabled();
    }

    @Override
    protected Ta getTa() {
        return new TalibRSI();
    }

}

