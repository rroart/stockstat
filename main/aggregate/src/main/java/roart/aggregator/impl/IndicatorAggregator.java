package roart.aggregator.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.IndicatorAggregator.AfterBeforeLimit;
import roart.aggregator.impl.IndicatorAggregator.SubType;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;
import roart.ml.common.MLClassifyModel;
import roart.pipeline.common.aggregate.Aggregator;
import roart.talib.util.TaConstants;

public abstract class IndicatorAggregator extends Aggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final int CMNTYPE = 0;
    protected static final int NEGTYPE = 1;
    protected static final int POSTYPE = 2;
    protected static final String CMNTYPESTR = "cmn";
    protected static final String NEGTYPESTR = "neg";
    protected static final String POSTYPESTR = "pos";

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    protected Map<String, double[][]> listMap;

    public IndicatorAggregator(MyMyConfig conf, String string, int category) {
        super(conf, string, category);
    }
    
    protected abstract List<SubType> wantedSubTypes();
    
    protected abstract Map<String, double[][]> getListMap();

    protected void printSignChange(String txt, String id, Map<Integer, Integer> posneg, boolean positive, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
    }

    protected String[] neg = { labelFN, labelTN };
    protected String[] pos = { labelTP, labelFP };
    protected String[][] posnegs = { neg, pos };
    protected String[] posneg = { POSTYPESTR, NEGTYPESTR };
    
    protected Map<String, Double> createLabelMap2() {
        Map<String, Double> labelMap2 = new HashMap<>();
        labelMap2.put(labelTP, 1.0);
        labelMap2.put(labelFP, 2.0);
        labelMap2.put(labelTN, 3.0);
        labelMap2.put(labelFN, 4.0);
        return labelMap2;
    }

    protected Map<Double, String> createLabelMap1() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, labelTP);
        labelMap1.put(2.0, labelFP);
        labelMap1.put(3.0, labelTN);
        labelMap1.put(4.0, labelFN);
        return labelMap1;
    }

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, Constants.TP);
        labelMap1.put(2.0, Constants.FP);
        labelMap1.put(3.0, Constants.TN);
        labelMap1.put(4.0, Constants.FN);
        return labelMap1;
    }

    /**
     * 
     * @param labelMapShort
     * @param range
     * @param afterbefore
     * @return a complex map
     * 
     * return the recent ones who need classification
     * offsetmap, a map from subtype to map with stock id to single value array
     * a map from subtype posnegcmn to map with stock id to value array
     * 
     */
    
    protected Map<String, Map<String, double[]>> getNewestPosNeg(Map<Double, String> labelMapShort, int[] range, AfterBeforeLimit afterbefore, Map<String, Object[]> taMap) {
        // calculate sections and do ML
        // a map from h/m + com/neg/sub to map<id, values>
        Map<String, Map<String, double[]>> mapIdMap = new HashMap<>();
        for (Entry<String, double[][]> entry : getListMap().entrySet()) {
            double[][] list = entry.getValue();
            //Double[] origMain = Arrays.copyOf(list[0], list[0].length);
            //list[0] = ArraysUtil.getPercentizedPriceIndex(list[0]);
            Object[] objs = taMap.get(entry.getKey());
            int begOfArray = (int) objs[range[0]];
            int endOfArray = (int) objs[range[1]];
            double[] trunclist = ArraysUtil.getSubExclusive(list[0], begOfArray, begOfArray + endOfArray);
            //Double[] trunclistOrig = ArraysUtil.getSubExclusive(origMain, begOfArray, begOfArray + endOfArray);
            if (endOfArray == 0) {
                continue;
            }
            List<SubType> subTypes = wantedSubTypes();
            for (SubType subType : subTypes) {
                double[] anArray = (double[]) objs[subType.getArrIdx()];
                getMlMappings(subType.getName(), subType.getType(), labelMapShort, mapIdMap, entry.getKey(), anArray, endOfArray, trunclist, afterbefore);
            }
        }
        return mapIdMap;
    }

    /**
     * 
     * @param labelMapShort
     * @param range
     * @param afterbefore
     * @return a complex map
     * 
     * return 
     * a map from stockid to map with Pair(subtype posnegcmn) to map with range to classification array
     * 
     */
    
    protected Map<String, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>>> getPosNeg(Map<Double, String> labelMapShort, AfterBeforeLimit afterbefore) {
        // calculate sections and do ML
        // a map from h/m + com/neg/sub to map<id, values>
        Map<String, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>>> mapIdMap = new HashMap<>();
        for (Entry<String, double[][]> entry : getListMap().entrySet()) {
            double[][] list = entry.getValue();
            //Double[] origMain = Arrays.copyOf(list[0], list[0].length);
            //list[0] = ArraysUtil.getPercentizedPriceIndex(list[0]);
            Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> mapMap = mapGetter(mapIdMap, entry.getKey());
            List<SubType> subTypes = wantedSubTypes();
            for (SubType subType : subTypes) {
                Object[] objs = subType.taMap.get(entry.getKey());
                int begOfArray = (int) objs[subType.range[0]];
                int endOfArray = (int) objs[subType.range[1]];
                double[] trunclist = ArraysUtil.getSubExclusive(list[0], begOfArray, begOfArray + endOfArray);
                //Double[] trunclistOrig = ArraysUtil.getSubExclusive(origMain, begOfArray, begOfArray + endOfArray);
                if (endOfArray == 0) {
                    continue;
                }
                double[] anArray = (double[]) objs[subType.getArrIdx()];
                getMlMappings(subType.getName(), subType, labelMapShort, mapMap, entry.getKey(), anArray, trunclist, afterbefore);
            }
        }
        return mapIdMap;
    }

    /**
     * 
     * @param mapMap
     * @param subType
     * @param commonType
     * @param posnegType
     * @param id
     * @param list
     * @param labelMap2
     * @param array
     * @param listsize
     * @param posneg
     * @param labels
     * @param afterbefore
     * 
     * returns 
     * 
     */
    
    protected void getPosNegMap(Map<String, Map<double[], Double>> mapMap, String subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, AfterBeforeLimit afterbefore) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.before, afterbefore.after, listsize);
        for (Entry<Integer, Integer> entry : newPosNeg.entrySet()) {
            int end = entry.getValue();
            String textlabel;
            if (list[end] < list[end + afterbefore.after]) {
                textlabel = labels[0];
            } else {
                textlabel = labels[1];
            }
            log.debug("{}: {} at {}", textlabel, id, end);
            printme(textlabel, end, list, array, afterbefore);
            double[] truncArray = ArraysUtil.getSub(array, entry.getKey(), end);
            Double doublelabel = labelMap2.get(textlabel);
            String commonMapName = subType + commonType;
            String posnegMapName = subType + posnegType;
            Map<double[], Double> commonMap = mapGetter(mapMap, commonMapName);
            Map<double[], Double> posnegMap = mapGetter(mapMap, posnegMapName);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    /**
     * 
     * @param mapIdMap
     * @param subType
     * @param commonType
     * @param posnegType
     * @param id
     * @param list
     * @param labelMap2
     * @param array
     * @param listsize
     * @param posneg
     * @param labels
     * @param afterbefore
     * 
     * returns 
     * 
     */
    
    protected void getPosNegMap(Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> mapIdMap, SubType subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, AfterBeforeLimit afterbefore) {
        if (true) return;
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.after, listsize);
        for (Entry<Integer, Integer> entry : posneg.entrySet()) {
            int end = entry.getValue();
            String textlabel;
            if (list[end] < list[end + afterbefore.after]) {
                textlabel = labels[0];
            } else {
                textlabel = labels[1];
            }
            log.debug("{}: {} at {}", textlabel, id, end);
            printme(textlabel, end, list, array, afterbefore);
            //double[] truncArray = ArraysUtil.getSub(array, entry.getKey(), end);
            Double doublelabel = labelMap2.get(textlabel);
            //String commonMapName = subType + commonType;
            //String posnegMapName = subType + posnegType;
            Map<Pair<Integer, Integer>, Double> commonMap = mapGetter3(mapIdMap, new Pair(subType, commonType));
            Map<Pair<Integer, Integer>, Double> posnegMap = mapGetter3(mapIdMap, new Pair(subType, posnegType));
            commonMap.put(new Pair(entry.getKey(), entry.getValue()), doublelabel);
            posnegMap.put(new Pair(entry.getKey(), entry.getValue()), doublelabel);
        }
    }

    /**
     * 
     * @param conf
     * @param range
     * @param afterbefore
     * @param objectMaps
     * @param otherResultMaps
     * @return a complex map
     * 
     * maps for learning
     * a map from the id subtype short name + posnegcmn to a map of a value list to labels
     * 
     */
    
    protected Map<String, Map<double[], Double>> createPosNegMaps(MyMyConfig conf) {
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
        for (String id : getListMap().keySet()) {

            double[][] list = getListMap().get(id);
            log.debug("t {}", Arrays.toString(list[0]));
            log.debug("listsize {}", list.length);
            /*
            if (conf.wantPercentizedPriceIndex() && list[0].length > 0) {
                list[0] = ArraysUtil.getPercentizedPriceIndex(list[0]);
            }
            */
            log.debug("list {} {} ", list.length, Arrays.asList(list));
            if (conf.wantML()) {
                Map<String, Double> labelMap2 = createLabelMap2();
                // also macd
                List<SubType> subTypes = wantedSubTypes();
                for (SubType subType : subTypes) {
                    Map<String, Double[]> anOtherResultMap = subType.resultMap;
                    Double[] curResult = anOtherResultMap.get(id);
                    if (curResult == null) {
                        log.debug("no macd for id {}", id);
                    }
                    if (curResult == null || !Arrays.stream(curResult).allMatch(i -> i != null)) {
                        continue;
                    }
                    Map<String, Object[]> taObjectMap = subType.taMap;
                    Object[] taObject = taObjectMap.get(id);
                    int begOfArray = (int) taObject[subType.range[0]];
                    int endOfArray = (int) taObject[subType.range[1]];
                    log.debug("beg end {} {} {}", id, begOfArray, endOfArray);
                    if (endOfArray <= 0) {
                        log.error("error arrayend 0");
                        continue;
                    }
                    double[] trunclist = ArrayUtils.subarray(list[0], begOfArray, begOfArray + endOfArray);
                    log.debug("trunclist {} {}", list.length, Arrays.asList(trunclist));

                    double[] anArray = (double[]) taObject[subType.getArrIdx()];
                    for (int i = 0; i < posneg.length; i++) {
                        Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(anArray, endOfArray, subType.filters[i].limit);
                        // instead of posneg, take from filter
                        getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, posneg[i], id, trunclist, labelMap2, anArray, trunclist.length, map[i], posnegs[i], subType.afterbefore);
                    }
                }
            }
        }
        return mapMap;
    }

    protected void printme(String label, int end, double[] values, double[] array, AfterBeforeLimit afterbefore) {
        StringBuilder me1 = new StringBuilder();
        StringBuilder me2 = new StringBuilder();
        for (int i = end - 3; i <= end + afterbefore.after; i++) {
            if ( i < 0 ) {
                int jj = 0;
                return;
            }
            me1.append(values[i] + " ");
            me2.append(array[i] + " ");
        }
        String m1 = me1.toString();
        String m2 = me2.toString();
        log.debug("me1 {}", m1);
        log.debug("me2 {}", m2);
    }

    private void getMlMappings(String name, String subType, Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap,
            String id, double[] array, int endOfArray,
            double[] valueList, AfterBeforeLimit afterbefore) {
        Map<String, double[]> offsetMap = IndicatorAggregator.mapGetter(mapIdMap, subType);
        Map<String, double[]> commonMap = IndicatorAggregator.mapGetter(mapIdMap, subType + CMNTYPESTR);
        Map<String, double[]> posMap = IndicatorAggregator.mapGetter(mapIdMap, subType + POSTYPESTR);
        Map<String, double[]> negMap = IndicatorAggregator.mapGetter(mapIdMap, subType + NEGTYPESTR);
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray);
        for (int i = 0; i < map.length; i++) {
        Map<Integer, Integer> posneg = map[0];
        Map<Integer, Integer> newPosneg = ArraysUtil.getFreshRanges(posneg, afterbefore.before, afterbefore.after, valueList.length);

        /*
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, afterbefore.before, afterbefore.after, valueList.length);
        */
        printSignChange(name, id, newPosneg, i == 0, endOfArray, afterbefore.after, labelMapShort);
        //printSignChange(name, id, newNeg, false, endOfArray, afterbefore.after, labelMapShort);
        if (/*!newNeg.isEmpty() ||*/ !newPosneg.isEmpty()) {
            int start = 0;
            int end = 0;
            /*
            if (!newNeg.isEmpty()) {
                start = newNeg.keySet().iterator().next();
                end = newNeg.get(start);
            }
            */
            if (!newPosneg.isEmpty()) {
                start = newPosneg.keySet().iterator().next();
                end = newPosneg.get(start);
            }
            if (end + 1 >= endOfArray) {
                continue;
            }
            double[] doubleArray = new double[] { endOfArray - end };
            offsetMap.put(id, doubleArray);
            log.debug("t {} {} {}", subType, id, valueList[end]);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            commonMap.put(id, truncArray);
            /*
            if (!newNeg.isEmpty()) {
                negMap.put(id, truncArray); 
            }
            */
            Map<String, double[]> aMap = i == 0 ? posMap : negMap;
            if (!newPosneg.isEmpty()) {
                aMap.put(id, truncArray);
            }
        }
        }
    }

    private void getMlMappings(String name, SubType subType, Map<Double, String> labelMapShort, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> mapIdMap,
            String id, double[] array,
            double[] valueList, AfterBeforeLimit afterbefore) {
        Map<String, Object[]> taObjectMap = subType.taMap;
        Object[] taObject = taObjectMap.get(id);
        int begOfArray = (int) taObject[subType.range[0]];
        int endOfArray = (int) taObject[subType.range[1]];

        //Map<Pair<Integer, Integer>, Double> posMap = IndicatorAggregator.mapGetter(mapIdMap, new Pair(subType, POSTYPESTR));
        //Map<Pair<Integer, Integer>, Double> negMap = IndicatorAggregator.mapGetter(mapIdMap, new Pair(subType, NEGTYPESTR));
        for (int j = 0; j < 2; j++) {
            //Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(array, endOfArray);
            Map<Integer, Integer>[] maps = ArraysUtil.searchForwardLimit(array, endOfArray, subType.filters[j].limit);
            for (int i = 0; i < 2; i++) {
                Map<Integer, Integer> posnegm = maps[i];
                Map<Integer, Integer> newPosneg = ArraysUtil.getFreshRanges(posnegm, afterbefore.before, afterbefore.after, valueList.length);

                double[] trunclist = valueList;
                Map<String, Double> labelMap2 = createLabelMap2();
                double[] anArray = (double[]) taObject[subType.getArrIdx()];
                getPosNegMap(mapIdMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, anArray, trunclist.length, maps[i], posnegs[i], subType.afterbefore);
            }
        }
    }

    protected void doSomething(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap2, AfterBeforeLimit afterbefore) {
        Map<String, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>>> m = getPosNeg(labelMapShort, afterbefore);
        Map<Double, String> reverseClassification = createLabelMapShort();
        // map from h/m + posnegcom to map<model, results>
        for (Entry<String, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>>> entry : m.entrySet()) {
            String id = entry.getKey();
            Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> subTypeMaps = entry.getValue();
            for (Entry<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> subTypeMapEntry : subTypeMaps.entrySet()) {
                Pair<SubType, String> pair = subTypeMapEntry.getKey();
                SubType subType = pair.getFirst();
                String posnegcmn = pair.getSecond();
                Map<Pair<Integer, Integer>, Double> classifyMap = subTypeMapEntry.getValue();
                for (Entry<Pair<Integer, Integer>, Double> entryClassification : classifyMap.entrySet()) {
                   Pair<Integer, Integer> range = entryClassification.getKey();
                   int start = range.getFirst();
                   int end = range.getSecond();
                   Double cls = entryClassification.getValue();
                   String tfpn = reverseClassification.get(cls);
                }
            }
        }
    }
    
    static <K, V> Map<K, V> mapGetter(Map<String, Map<K, V>> mapMap, String key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    static <K, V> Map<K, V> mapGetter2(Map<Pair, Map<K, V>> mapMap, Pair key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    static <K, V, P> Map<K, V> mapGetter3(Map<P, Map<K, V>> mapMap, P key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    class AfterBeforeLimit {
        public int before;
        public int after;
        
        public AfterBeforeLimit(int before, int after) {
            this.before = before;
            this.after = after;
        }
    }

    class Filter {
        public boolean aboveEqual;
        public double limit;
        public String[] texts;
        
        public Filter(boolean aboveEqual, double limit, String[] texts) {
            this.aboveEqual = aboveEqual;
            this.limit = limit;
            this.texts = texts;
        }
    }
    
    protected abstract class SubType {
        public abstract String getType();
        public abstract String getName();
        public abstract int getArrIdx();
        //public abstract int getIdx();
        //public abstract Filter getFilter();
        protected Map<String, Double[][]> listMap;
        protected Map<String, Double[]> resultMap;
        protected Map<String, Object[]> taMap;
        protected AfterBeforeLimit afterbefore;
        protected int[] range;
        protected Filter[] filters;
    }

}
