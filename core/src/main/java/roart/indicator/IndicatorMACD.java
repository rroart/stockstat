package roart.indicator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyConfig;
import roart.model.Stock;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class IndicatorMACD extends Indicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;

    public IndicatorMACD(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
    }

    @Override
    public boolean isEnabled() {
        return conf.isMACDenabled();
    }

    @Override
    public Object getResultItem(Stock stock) {
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String id = stock.getId();
        Pair pair = new Pair(market, id);
        Set ids = new HashSet();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            System.out.println("key " + key + " : " + periodDataMap.keySet());
            log.info("key " + key + " : " + periodDataMap.keySet());
        }
        double momentum = tu.getMom(conf.getDays(), market, id, ids, marketdatamap, perioddata, periodstr);
        return momentum;
    }
}

