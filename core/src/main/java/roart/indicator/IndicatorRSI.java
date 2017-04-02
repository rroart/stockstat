package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyConfig;
import roart.model.StockItem;
//import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;

public class IndicatorRSI extends Indicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, List<Double>> listMap;
    Map<String, Double[]> resultMap;

    public IndicatorRSI(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        calculateRSIs(conf, marketdatamap, periodDataMap, category);        
    }

	private void calculateRSIs(MyConfig conf, Map<String, MarketData> marketdatamap,
			Map<String, PeriodData> periodDataMap, int category) throws Exception {
		SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        this.listMap = StockDao.getArr(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
        log.info("time0 " + (System.currentTimeMillis() - time0));
        long time1 = System.currentTimeMillis();
        resultMap = new HashMap();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        for (String id : listMap.keySet()) {
        	/*
        	List<Double> list = listMap.get(id);
            Pair<String, String> pair = new Pair<>(market, id);
            Set<Pair<String, String>> ids = new HashSet<>();
            ids.add(pair);
            double rsi = tu.getRSI2(conf.getDays(), market, id, ids, marketdatamap, perioddata, periodstr);
            */
            List<Double> list = listMap.get(id);
            Double[] rsi = tu.getRSI(list, conf.getDays(), conf.isRSIDeltaEnabled(), conf.getRSIDeltaDays());
            resultMap.put(id, rsi);
            if (id.equals("EUCA000520")) {
                //log.info("ind list + " + list);
                //log.info("ind out " + Arrays.toString(rsi));
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
	}

    @Override
    public boolean isEnabled() {
        return conf.isRSIEnabled();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
    	TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new Pair(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            System.out.println("key " + key + " : " + periodDataMap.keySet());
            log.info("key " + key + " : " + periodDataMap.keySet());
        }
        Double[] rsi = resultMap.get(id);
        return rsi;
    }

    @Override
    public Object[] getResultItemTitle() {
    	int size = 1;
    	if (conf.isRSIDeltaEnabled()) {
    		size++;
    	}
    	Object[] objs = new Object[size];
    	objs[0] = title;
    	if (conf.isRSIDeltaEnabled()) {
    		objs[1] = Constants.DELTA + title;
    	}
        return objs;
    }

}

