package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyConfig;
import roart.db.DbSpark;
import roart.model.StockItem;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;
import scala.collection.mutable.WrappedArray;

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
        try{
            Map resultMap2; 
        resultMap2 = DbSpark.getMe(listMap, this);
        } catch(Exception e) {
            log.info("Exception", e);
        }
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
    public Object calculate(Object as) {
        TaUtil tu = new TaUtil();
        log.info("myclass " + as.getClass().getName());
        WrappedArray wa = (WrappedArray) as;
        Double[] arr2 = (Double[]) wa.array();
        log.info("myclass " + arr2.getClass().getName());
        //Double[] arr = (Double[]) as;
        List<Double> list = Arrays.asList(arr2);
        Double[] momentum = tu.getMomAndDelta(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
        return momentum;
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
    	int size = 2;
    	if (conf.isMACDDeltaEnabled()) {
    		size++;
    	}
    	if (conf.isMACDHistogramDeltaEnabled()) {
    		size++;
    	}
    	Object[] objs = new Object[size];
    	int retindex = 0;
    	objs[retindex++] = title + " " + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + " " + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + " " + "mom";
        if (conf.isMACDDeltaEnabled()) {
    		objs[retindex++] = title + " " + Constants.DELTA + "mom";
    	}
        return objs;
    }

}

