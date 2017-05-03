package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.tictactec.ta.lib.MInteger;

import roart.config.MyConfig;
import roart.db.DbSpark;
import roart.model.StockItem;
import roart.util.ArraysUtil;
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
    Map<String, Object[]> objectMap;
    //Map<String, Double> resultMap;
    Map<String, Object[]> resultMap;
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Object[] emptyField;

    private int fieldSize = 0;
    
    /**
     *  days before positive/negative change
     * @return
     */
    private int getDaysBeforeZero() {
        return 25;
    }
    
    /**
     *  days after positive/negative change
     * @return
     */
   private int getDaysAfterZero() {
        return 10;
    }
    
    private boolean wantScore() {
        return true;
    }

    private boolean wantML() {
        return true;
    }
    
    private boolean wantMCP() {
        return true;
    }
    
    private boolean wantLR() {
        return true;
    }
    
    private int weightBuyHist() {
        return 25;
    }

    private int weightBuyHistDelta() {
        return 25;
    }

    private int weightBuyMacd() {
        return 0;
    }

    private int weightBuyMacdDelta() {
        return 25;
    }

    private int weightSellHist() {
        return 25;
    }

    private int weightSellHistDelta() {
        return 25;
    }

    private int weightSellMacd() {
        return 0;
    }

    private int weightSellMacdDelta() {
        return 25;
    }

    public IndicatorMACD(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        fieldSize = fieldSize();
        calculateMomentums(conf, marketdatamap, periodDataMap, category);        
    }

    // TODO make an oo version of this
    private void calculateMomentums(MyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        this.listMap = StockDao.getArr(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        objectMap = new HashMap<>();
        momMap = new HashMap<>();
        buyMap = new HashMap<>();
        sellMap = new HashMap<>();
        List<Double> macdList = new ArrayList<>();
        List<Double> histList = new ArrayList<>();
        List<Double> macdDList = new ArrayList<>();
        List<Double> histDList = new ArrayList<>();
        //int daysAfterZero = 10;
        //int daysBeforeZero = 25;
        try {
            long time2 = System.currentTimeMillis();
            Map<String, Object[]> m; 
            m = DbSpark.doCalculations(listMap, this);
            if (m != null) {
                log.info("time2 " + (System.currentTimeMillis() - time2));
                for (String key : m.keySet()) {
                    log.info("key " + key);
                    log.info("value " + Arrays.toString(m.get(key)));
                }
                objectMap = m;
                //if (true) return;
            }
        } catch(Exception e) {
            log.info("Exception", e);
        }
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        Map<double[], Double> commonMap = new HashMap<>();
        Map<double[], Double> posMap = new HashMap<>();
        Map<double[], Double> negMap = new HashMap<>();
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
            Object[] objs = tu.getMomAndDeltaFull(list, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
            objectMap.put(id, objs);
            Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs);
            momMap.put(id, momentum);
            Map<String, List<Double>> retMap = new HashMap();
            //Object[] full = tu.getMomAndDeltaFull(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
            {
                double[] macd = (double[]) objs[0];
                double[] hist = (double[]) objs[2];
                log.info("outout1 " + Arrays.toString(macd));
                log.info("outout3 " + Arrays.toString(hist));
            }
            MInteger begOfArray = (MInteger) objs[3];
            MInteger endOfArray = (MInteger) objs[4];

            log.info("beg end " + id + " "+ begOfArray.value + " " + endOfArray.value);
            log.info("list " + list);
            List<Double> trunclist = list.subList(begOfArray.value, begOfArray.value + endOfArray.value);
            log.info("trunclist"  + trunclist);
            if (true || id.equals("F00000NMNP")){

                if (momentum[0] != null) {
                    histList.add(momentum[0]);
                }
                if (momentum[1] != null) {
                    histDList.add(momentum[1]);
                }
                if (momentum[2] != null) {
                    macdList.add(momentum[2]);
                }
                if (momentum[3] != null) {
                    macdDList.add(momentum[3]);
                }
                //System.out.println("outout2 " + Arrays.toString(sig));
                List<StockItem> datedstocklists[] = marketdatamap.get(market).datedstocklists;
                int index = 0;
                if (false && index >= 0) {
                    for (int i = index; i < datedstocklists.length; i++) {
                        List<StockItem> stocklist = datedstocklists[i];
                        for (StockItem stock : stocklist) {
                            String stockid = stock.getId();
                            Double value = StockDao.getValue(stock, category);
                            if (value != null) {
                                StockDao.mapAdd(retMap, stockid, value);
                            }
                        }
                    }
                }
            }
            if (wantML()) {
                Map<Double, String> labelMapShort = createLabelMapShort();
                Map<Double, String> labelMap1 = createLabelMap1();
                Map<String, Double> labelMap2 = createLabelMap2();
                //List<Double> list = listMap.get(id);
                //Double[] momentum = resultMap.get(id);
                double[] macdarr = (double[]) objs[0];
                double[] histarr = (double[]) objs[2];
                int momentumSize = momentum.length;
                double hist = momentum[0];
                double histd = momentum[1];
                double macd = momentum[2];
                double macdd = momentum[3];
                double lasthist = hist;
                int num = 0;
                int numother = 0;
                //out:
                //for (int i = trunclist.size(); i >=0 ; i --) {
                //for (String id : listMap.keySet()) {
                int trunclistsize = trunclist.size();
                Map<Integer, Integer>[] map = ArraysUtil.searchForward(histarr);
                Map<Integer, Integer> pos = map[0];
                Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, getDaysBeforeZero(), getDaysAfterZero(), trunclistsize);
                for (int start : newPos.keySet()) {
                    int end = newPos.get(start);
                    String label = null;
                    try {
                        if (trunclist.get(end) < trunclist.get(end + getDaysAfterZero())) {
                            label = "FalseNegative";
                            log.info("FalseNegative: " + id);
                        } else {
                            label = "TrueNegative";
                        }
                    } catch (Exception e) {
                        log.error("myexcept " + pos + " : " + newPos + " " + start + " " + end + " " + trunclist.size() + " " + histarr.length, e);
                    }
                    double[] truncArray = ArraysUtil.getSub(histarr, start, end);
                    Double label2 = labelMap2.get(label);
                    commonMap.put(truncArray, label2);
                    posMap.put(truncArray, label2);
                }
                Map<Integer, Integer> neg = map[1];
                Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, getDaysBeforeZero(), getDaysAfterZero(), trunclistsize);
                for (int start : newNeg.keySet()) {
                    int end = newNeg.get(start);
                    String label;
                    if (trunclist.get(end) < trunclist.get(end + getDaysAfterZero())) {
                        label = "FalsePositive";
                        log.info("FalsePositive: " + id);
                    } else {
                        label = "TruePositive";
                    }
                    double[] truncArray = ArraysUtil.getSub(histarr, start, end);
                    Double label2 = labelMap2.get(label);
                    commonMap.put(truncArray, label2);
                    negMap.put(truncArray, label2);
                }
                //}
            }
        }

        log.info("histlist " + histList);
        Double maxhist = 0.0;
        Double maxdhist = 0.0;
        Double maxmacd = 0.0;
        Double maxdmacd = 0.0;
        Double minhist = 0.0;
        Double mindhist = 0.0;
        Double minmacd = 0.0;
        Double mindmacd = 0.0;
        log.info("listsize" + histList.size());
        log.info("listsize" + histDList.size());
        log.info("listsize" + macdList.size());
        log.info("listsize" + macdDList.size());
        if (!histList.isEmpty()) {
            maxhist = Collections.max(histList);
        }
        if (!histDList.isEmpty()) {
            maxdhist = Collections.max(histDList);
        }
        if (!macdList.isEmpty()) {
            maxmacd = Collections.max(macdList);
        }
        if (!macdDList.isEmpty()) {
            maxdmacd = Collections.max(macdDList);
        }
        if (!histList.isEmpty()) {
            minhist = Collections.min(histList);
        }
        if (!histDList.isEmpty()) {
            mindhist = Collections.min(histDList);
        }
        if (!macdList.isEmpty()) {
            minmacd = Collections.min(macdList);
        }
        if (!macdDList.isEmpty()) {
            mindmacd = Collections.min(macdDList);
        }
        // find recommendations
        Map<Double, String> labelMapShort = createLabelMapShort();
        for (String id : listMap.keySet()) {
            if (wantScore()) {
                Double[] momentum = momMap.get(id);
                int momentumSize = momentum.length;
                double hist = momentum[0];
                double histd = momentum[1];
                double macd = momentum[2];
                double macdd = momentum[3];
                int momidx = 4;
                if (hist >= 0) {
                    double recommend = weightBuyHist()*(maxhist - hist)/maxhist;
                    recommend += weightBuyHistDelta()*(maxdhist - histd)/maxdhist;
                    if (true) {
                        recommend += weightBuyMacd()*(maxmacd - macd)/maxmacd;
                        recommend += weightBuyMacdDelta()*(maxdmacd - macdd)/maxdmacd;
                    }
                    buyMap.put(id, recommend);
                }
                momidx++;
                if (hist < 0) {
                    double recommend = weightSellHist()*(minhist - hist)/minhist;
                    recommend += weightSellHistDelta()*(mindhist - histd)/mindhist;
                    if (true) {
                        recommend += weightSellMacd()*(minmacd - macd)/minmacd;
                        recommend += weightSellMacdDelta()*(mindmacd - macdd)/mindmacd;
                    }
                    sellMap.put(id, recommend);
                    //momentum[momidx] = recommend;
                }
                momidx++;
            }
        }
        try {
            String model = "MultilayerPerceptronClassifier";
            DbSpark.learntest(commonMap, model, getDaysBeforeZero(), key, "common", 4);
            DbSpark.learntest(posMap, model, getDaysBeforeZero(), key, "pos", 4);
            DbSpark.learntest(negMap, model, getDaysBeforeZero(), key, "neg", 4);
            model = "LogisticRegression";
            DbSpark.learntest(commonMap, model, getDaysBeforeZero(), key, "common", 4);
            DbSpark.learntest(posMap, model, getDaysBeforeZero(), key, "pos", 4);
            DbSpark.learntest(negMap, model, getDaysBeforeZero(), key, "neg", 4);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        // calculate sections and do ML
        Map<String, double[]> commonMap2 = new HashMap<>();
        Map<String, double[]> posMap2 = new HashMap<>();
        Map<String, double[]> negMap2 = new HashMap<>();
        for (String id : listMap.keySet()) {
            List<Double> list = listMap.get(id);
            Object[] objs = objectMap.get(id);
            //double[] macd = (double[]) objs[0];
            double[] histarr = (double[]) objs[2];
        MInteger begOfArray = (MInteger) objs[3];
        MInteger endOfArray = (MInteger) objs[4];
            List<Double> trunclist = list.subList(begOfArray.value, begOfArray.value + endOfArray.value);
            int trunclistsize = trunclist.size();
            Map<Integer, Integer>[] map = ArraysUtil.searchForward(histarr);
            Map<Integer, Integer> pos = map[0];
            Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, getDaysBeforeZero(), getDaysAfterZero(), trunclistsize);
            Map<Integer, Integer> neg = map[1];
            printSignChange(id, map, trunclistsize, getDaysAfterZero());
            Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, getDaysBeforeZero(), getDaysAfterZero(), trunclistsize);
            if (!newNeg.isEmpty() && !newPos.isEmpty()) {
                int start = 0;
                int end = 0;
                if (!newNeg.isEmpty()) {
                    start = newNeg.keySet().iterator().next();
                    end = newNeg.get(start);
                }
                if (!newPos.isEmpty()) {
                    start = newPos.keySet().iterator().next();
                    end = newPos.get(start);
                }
                double[] truncArray = ArraysUtil.getSub(histarr, start, end);
                commonMap2.put(id, truncArray);
                if (!newNeg.isEmpty()) {
                    negMap2.put(id, truncArray); 
                }
                if (!newPos.isEmpty()) {
                    posMap2.put(id, truncArray);
                }
            }
            /*
            Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, daysBeforeZero, daysAfterZero, trunclistsize);
            if (!newNeg.isEmpty() && !newPos.isEmpty()) {
                int posint = 0;
                int negint = 0;
                int start = 0;
                int end = 0;
                if (!newNeg.isEmpty()) {
                    negint = Collections.max(newNeg.keySet());
                    start = negint;
                    end = newNeg.get(start);
                }
                if (!newPos.isEmpty()) {
                    posint = Collections.max(newPos.keySet());
                    start = posint;
                    end = newPos.get(start);
                }
                double[] truncArray = ArraysUtil.getSub(histarr, start, end);
                if (posint > negint) {
                    posMap2.put(id, truncArray);
                } else {
                    negMap2.put(id,  truncArray);
                }
                commonMap2.put(id, truncArray);
            }
            */
        }
        String model = "MultilayerPerceptronClassifier";
        Map<String, Double[]> map1 = DbSpark.classify(commonMap2, model, getDaysBeforeZero(), key, "common", 4, labelMapShort);
        Map<String, Double[]> map2 = DbSpark.classify(posMap2, model, getDaysBeforeZero(), key, "pos", 4, labelMapShort);
        Map<String, Double[]> map3 = DbSpark.classify(negMap2, model, getDaysBeforeZero(), key, "neg", 4, labelMapShort);
        model = "LogisticRegression";
        Map<String, Double[]> map4 = DbSpark.classify(commonMap2, model, getDaysBeforeZero(), key, "common", 4, labelMapShort);
        Map<String, Double[]> map5 = DbSpark.classify(posMap2, model, getDaysBeforeZero(), key, "pos", 4, labelMapShort);
        Map<String, Double[]> map6 = DbSpark.classify(negMap2, model, getDaysBeforeZero(), key, "neg", 4, labelMapShort);
        List<Map> maplist = new ArrayList<>();
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            Object[] objs = objectMap.get(id);
            Object[] fields = new Object[fieldSize];
            momMap.put(id, momentum);
            resultMap.put(id, fields);
            int retindex = tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            Double buy = buyMap.get(id);
            fields[retindex++] = buy;
            Double sell = sellMap.get(id);
            fields[retindex++] = sell;
            if (wantScore()) {
                //int momidx = 6;
                Double[] type;
                type = map1.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
                type = map2.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
                type = map3.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
               type = map4.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
               type = map5.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
               type = map6.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                printout(type, id);
                fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
        
    }

    private void printSignChange(String id, Map<Integer, Integer>[] map, int trunclistsize, int i) {
        Map<Integer, Integer> pos = map[0];
        if (!pos.isEmpty()) {
       int posmaxind = Collections.max(pos.keySet());
        int posmax = pos.get(posmaxind);
        if (posmax + i >= trunclistsize) {
            System.out.println("Sign changed to negative for id " + id + " " + title + " since " + (trunclistsize - posmax));
        }
    }
        Map<Integer, Integer> neg = map[1];
        if (!neg.isEmpty()) {
        int negmaxind = Collections.max(neg.keySet());
        int negmax = neg.get(negmaxind);
        if (negmax + i >= trunclistsize) {
            System.out.println("Sign changed to positive for id " + id + " " + title + "since " + (trunclistsize - negmax));
        }
        }
    }

    private void printout(Double[] type, String id) {
        if (type != null) {
        System.out.println("Type " + type + " id " + id);
        }
    }
    
    private Map<String, Double> createLabelMap2() {
        Map<String, Double> labelMap2 = new HashMap<>();
        labelMap2.put("TruePositive", 1.0);
        labelMap2.put("FalsePositive", 2.0);
        labelMap2.put("TrueNegative", 3.0);
        labelMap2.put("FalseNegative", 4.0);
        return labelMap2;
    }

    private Map<Double, String> createLabelMap1() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, "TruePositive");
        labelMap1.put(2.0, "FalsePositive");
        labelMap1.put(3.0, "TrueNegative");
        labelMap1.put(4.0, "FalseNegative");
        return labelMap1;
    }

    private Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, "TP");
        labelMap1.put(2.0, "FP");
        labelMap1.put(3.0, "TN");
        labelMap1.put(4.0, "FN");
        return labelMap1;
    }

    @Override
    public Object calculate(Object as) {
        TaUtil tu = new TaUtil();
        //log.info("myclass " + as.getClass().getName());
        WrappedArray wa = (WrappedArray) as;
        Double[] arr2 = (Double[]) wa.array();
        //log.info("myclass " + arr2.getClass().getName());
        //Double[] arr = (Double[]) as;
        List<Double> list = Arrays.asList(arr2);
        //Double[] momentum = tu.getMomAndDelta(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
        Object[] objs = tu.getMomAndDeltaFull(list, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
        return objs;
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
        Object[] result = resultMap.get(id);
        if (result == null) {
            /*
            Double[] i = resultMap.values().iterator().next();
            int size = i.length;
            momentum = new Double[size];
             */
            result = emptyField;
        }
        return result;
    }

    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
         int retindex = 0;
        objs[retindex++] = title + " " + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + " " + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + " " + "mom";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + " " + Constants.DELTA + "mom";
        }
        if (wantScore()) {
            objs[retindex++] = title + " " + "buy";
            objs[retindex++] = title + " " + "sell";
        }
        if (wantMCP()) {
            String mpc = "";
            String val = "";
            //String mpc = "" + DbSpark.eval("MultilayerPerceptronClassifier", title, "common");
            val = "" + DbSpark.eval("MultilayerPerceptronClassifier", title, "common");
            objs[retindex++] = title + " " + "MPCcom"+val;
            val = "" + DbSpark.eval("MultilayerPerceptronClassifier", title, "neg");
           objs[retindex++] = title + " " + "MPCpos"+val;
           val = "" + DbSpark.eval("MultilayerPerceptronClassifier", title, "neg");
            objs[retindex++] = title + " " + "MPCneg"+val;
        }
        if (wantLR()) {
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = "" + DbSpark.eval("LogisticRegression", title, "common");
            objs[retindex++] = title + " " + "LRcom"+val;
            val = "" + DbSpark.eval("LogisticRegression", title, "pos");
            objs[retindex++] = title + " " + "LRpos"+val;
            val = "" + DbSpark.eval("LogisticRegression", title, "neg");
           objs[retindex++] = title + " " + "LRneg"+val;
            objs[retindex++] = title + " " + "LR prob";
        }
        //emptyField = new Double[size];
        log.info("fieldsizet " + retindex);
        return objs;
    }

    private int fieldSize() {
        int size = 2;
        if (conf.isMACDDeltaEnabled()) {
            size++;
        }
        if (conf.isMACDHistogramDeltaEnabled()) {
            size++;
        }
        if (wantScore()) {
            size += 2;
        }
        if (wantMCP()) {
            size += 3;
        }
        if (wantLR()) {
            size += 4;
        }
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }
}

