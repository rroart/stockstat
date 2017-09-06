package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyMyConfig;
import roart.model.StockItem;
//import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;

public class IndicatorSTOCH extends Indicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, List<Double>> listMap;
    Map<String, Double[]> resultMap;

    // TODO extend to three cats
    public IndicatorSTOCH(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        calculateSTOCHs(conf, marketdatamap, periodDataMap, category);        
    }

	private void calculateSTOCHs(MyMyConfig conf, Map<String, MarketData> marketdatamap,
			Map<String, PeriodData> periodDataMap, int category) throws Exception {
		SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // TODO three lists
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
            // TODO add low high later
            Double[] momentum = tu.getSTOCH(list, list, list, conf.getDays(), conf.isSTOCHDeltaEnabled(), conf.getSTOCHDeltaDays());
            resultMap.put(id, momentum);
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
	}

    @Override
    public boolean isEnabled() {
        return conf.isSTOCHEnabled();
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
        Double[] stoch = resultMap.get(id);
        return stoch;
    }

    @Override
    public Object[] getResultItemTitle() {
    	int size = 1;
    	if (conf.isSTOCHDeltaEnabled()) {
    		size++;
    	}
    	Object[] objs = new Object[size];
    	objs[0] = title;
    	if (conf.isSTOCHDeltaEnabled()) {
    		objs[1] = Constants.DELTA + title;
    	}
        return objs;
    }

    @Override
    protected Map<String, Object[]> getResultMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap,
            Map<String, Double[]> momMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Map<String, Double[]> getCalculatedMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap,
            Map<String, double[]> truncListMap) {
        // TODO Auto-generated method stub
        return null;
    }

}

