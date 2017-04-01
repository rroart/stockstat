package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyConfig;
import roart.model.StockItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;

public class IndicatorMACD extends Indicator {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, List<Double>> listMap;
    //Map<String, Double> resultMap;
    Map<String, Double[]> resultMap;

    public IndicatorMACD(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        calculateMomentums(conf, marketdatamap, periodDataMap, category);        
    }

	private void calculateMomentums(MyConfig conf, Map<String, MarketData> marketdatamap,
			Map<String, PeriodData> periodDataMap, int category) throws Exception {
		SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        this.listMap = StockDao.getArr(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
        log.info("time0 " + (System.currentTimeMillis() - time0));
        long time1 = System.currentTimeMillis();
        resultMap = new HashMap<>();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        for (String id : listMap.keySet()) {
        	if (id.equals("EUCA000520")) {
        	List<Double> list = listMap.get(id);
            Pair<String, String> pair = new Pair<>(market, id);
            Set<Pair<String, String>> ids = new HashSet<>();
            ids.add(pair);
            double momentum = tu.getMom(conf.getDays(), market, id, ids, marketdatamap, perioddata, periodstr);
        	}
            List<Double> list = listMap.get(id);
            if (id.equals("EUCA000520")) {
            	log.info("india list " + list);
            }
            //double momentum = tu.getMom(list, conf.getDays());
            Double[] momentum = tu.getMomAndDelta(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
            resultMap.put(id, momentum);
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
	}

    @Override
    public boolean isEnabled() {
        return conf.isMACDEnabled();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new Pair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            System.out.println("key " + key + " : " + periodDataMap.keySet());
            log.info("key " + key + " : " + periodDataMap.keySet());
        }
        //double momentum = resultMap.get(id);
        Double[] momentum = resultMap.get(id);
        return momentum;
    }
    
    @Override
    public Object[] getResultItemTitle() {
    	int size = 1;
    	if (conf.isMACDDeltaEnabled()) {
    		size++;
    	}
    	if (conf.isMACDHistogramDeltaEnabled()) {
    		size++;
    	}
    	Object[] objs = new Object[size];
    	objs[0] = title;
    	if (conf.isMACDDeltaEnabled()) {
    		objs[1] = Constants.DELTA + title;
    	}
    	if (conf.isMACDHistogramDeltaEnabled()) {
    		objs[2] = Constants.DELTA + "hist " + title;
    	}
        return objs;
    }

}

