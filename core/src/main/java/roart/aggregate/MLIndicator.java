package roart.aggregate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.tictactec.ta.lib.MInteger;

import roart.calculate.CalcNode;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.ConfigConstantMaps;
import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.ml.MLClassifyDao;
import roart.ml.MLClassifyModel;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.ResultMeta;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;
import scala.collection.mutable.WrappedArray;

public class MLIndicator extends Aggregator {

    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, Double[][]> listMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    Map<Pair, String> pairCatMap;
    /*
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    */
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;
    
    private final String MYTITLE = "comb";
    
    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        //map.put("MACD", momMap);
        return map;
    }
    
    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }
    
    public Map<String, Double[][]> getListMap() {
        return listMap;
    }
    
    private int fieldSize = 0;

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    @Override
    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> retMap = new HashMap<>();
        List<ResultItemTable> otherTables = new ArrayList<>();
        if (mlTimesTableRows != null) {
            retMap.put(ControlService.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(ControlService.EVENT, eventTableRows);
        }
        return retMap;
    }

    List<MLClassifyDao> mldaos = new ArrayList<>();

    public MLIndicator(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, MarketData> marketdatamap, 
            Map<String, PeriodData> periodDataMap, String title, int category, Category[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
        this.periodDataMap = periodDataMap;
        this.periodmap = periodmap;
        this.key = title;
        makeMapTypes();
        if (conf.wantML()) {
            if (conf.wantMLSpark()) {
                mldaos.add(new MLClassifyDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLClassifyDao("tensorflow", conf));
            }
        }
        fieldSize = fieldSize();
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
            Object[] objs = new Object[fieldSize];
            int retindex = 0;
            objs[retindex++] = "";
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMomentums(conf, marketdatamap, periodDataMap, category, categories, datareaders);        
        }
    }

    /*
    private abstract class MacdSubType {
        public abstract  String getType();
        public abstract  String getName();
        public abstract  int getArrIdx();
    }
    */
    
    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }
    
    private void makeMapTypes() {
        mapTypes.put(0, MYTITLE);
    }

    @Override
    public List<Integer> getTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }
    
    private Map<Integer, String> mapTypes = new HashMap<>();
    
    /*
    private List<MacdSubType> wantedSubTypes = new ArrayList<>();
    
   private List<MacdSubType> wantedSubTypes() {
          return wantedSubTypes;
    }
  */
    
   public Map<double[], Double> getEvaluations(MyMyConfig conf, int j, Object retObj[]) throws JsonParseException, JsonMappingException, IOException {
       int listlen = conf.getTableDays();
       List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
       Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
       //List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
       Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
       // find recommendations
       double recommend = 0;
       //transform(conf, buyList);
       if (indicatorMap == null) {
           return new HashMap<>();
       }
       Map<double[], Double> retMap = new HashMap<>();
       for (String id : indicatorMap.keySet()) {
           int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
           int curlistidx = listlen - 1 - j;
           Double[][] list = listList.get(0).get(id);
           if (list[0][newlistidx] == null || list[0][curlistidx] == null) {
               continue;
           }
           double change = list[0][newlistidx]/list[0][curlistidx] - 1;
           double[] merged = ArraysUtil.convert(indicatorMap.get(id));
           
           // cat 1.0 is for >= threshold, 2.0 is for belov
           Double cat = 2.0;
           if (change > conf.getAggregatorsIndicatorThreshold()) {
               cat = 1.0;
           }
           retMap.put(merged, cat);
           /*
           for (int i = 0; i < keys.size(); i++) {
               String key = keys.get(i);
               // TODO temp fix
               CalcNode node = (CalcNode) conf.configValueMap.get(key);
               //node.setDoBuy(useMax);
               double value = momrsi[i];
               recommend += node.calc(value, 0) * change;
           }
           */
       }
       return retMap;
   }

    // TODO make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category2, Category[] categories, Pipeline[] datareaders) throws Exception {
        Category cat = IndicatorUtils.getWantedCategory(categories);
        if (cat == null) {
            return;
        }
        category = cat.getPeriod();
        title = cat.getTitle();
        key = title;
        Map<String, Pipeline> pipelineMap = new HashMap<>();
        for (Pipeline datareader : datareaders) {
            pipelineMap.put(datareader.pipelineName(), datareader);
        }
        //Map<Pair, List<StockItem>> pairListMap;
        //Map<Pair, Map<Date, StockItem>> pairDateMap;
        //pairListMap = (Map<Pair, List<StockItem>>) pipelineMap.get(PipelineConstants.EXTRAREADER).getLocalResultMap().get(PipelineConstants.PAIRLIST);
        //pairCatMap = (Map<Pair, String>) pipelineMap.get(PipelineConstants.EXTRAREADER).getLocalResultMap().get(PipelineConstants.PAIRCATMAP);
        //pairDateMap = (Map<Pair, Map<Date, StockItem>>) pipelineMap.get(PipelineConstants.EXTRAREADER).getLocalResultMap().get(PipelineConstants.PAIRDATELIST);
        Pipeline extrareader = pipelineMap.get(PipelineConstants.EXTRAREADER);
        Map<String, Object> localResults =  extrareader.getLocalResultMap();
        Map<Pair, List<StockItem>> pairStockMap = (Map<Pair, List<StockItem>>) localResults.get(PipelineConstants.PAIRSTOCK);
        Map<Pair, Map<Date, StockItem>> pairDateMap = (Map<Pair, Map<Date, StockItem>>) localResults.get(PipelineConstants.PAIRDATE);
        Map<Pair, String> pairCatMap = (Map<Pair, String>) localResults.get(PipelineConstants.PAIRCAT);
        Map<Pair, Double[][]> pairListMap = (Map<Pair, Double[][]>) localResults.get(PipelineConstants.PAIRLIST);
        Map<Pair, List<Date>> pairDateListMap = (Map<Pair, List<Date>>) localResults.get(PipelineConstants.PAIRDATELIST);
        Map<Pair, double[][]> pairTruncListMap = (Map<Pair, double[][]>) localResults.get(PipelineConstants.PAIRTRUNCLIST);

        List<Date> dateList = (List<Date>) pipelineMap.get("" + this.category).getLocalResultMap().get(PipelineConstants.DATELIST);
        Map<String, Indicator> newIndicatorMap = new HashMap<>();
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();

        Map<String, List<AggregatorMLIndicator>> usedIndicators = AggregatorMLIndicator.getUsedAggregatorMLIndicators(conf);
        Set<String> ids = new HashSet<>();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[][]> list0 = (Map<String, Double[][]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
        ids.addAll(list0.keySet());
        //AggregatorMLIndicator.getI
        TaUtil tu = new TaUtil();
        Map<String, Indicator> indicatorMap = new HashMap<>();
       for (String type : usedIndicators.keySet()) {
            List<AggregatorMLIndicator> list = usedIndicators.get(type);
            for (AggregatorMLIndicator ind : list) {
                String indicator = ind.indicator();
                if (indicator != null) {
                indicatorMap.put(indicator, ind.getIndicator(marketdatamap, category, newIndicatorMap, usedIndicatorMap, datareaders));
                }
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                ids.retainAll(aResult.keySet());
            }
        }
       List<Indicator> indicators = new ArrayList<>(indicatorMap.values());

        DbAccess dbDao = DbDao.instance(conf);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = (Map<String, Double[][]>) list0;
        if (conf.wantPercentizedPriceIndex()) {
            
        }
        if (!anythingHere(listMap)) {
            System.out.println("empty"+key);
            return;
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        probabilityMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        otherMeta = new ArrayList<>();
        objectMap = new HashMap<>();
        /*
        momMap = new HashMap<>();
        buyMap = new HashMap<>();
        sellMap = new HashMap<>();
        */
        /*
        List<Double> macdLists[] = new ArrayList[4];
        for (int i = 0; i < 4; i ++) {
            macdLists[i] = new ArrayList<>();
        }
        */
        long time2 = System.currentTimeMillis();
        //System.out.println("imap " + objectMap.size());
        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();

        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
       // System.out.println("allids " + listMap.size());

        //Map<String, Double[]> list0 = (Map<String, Double[]>) cat.getResultMap().get("LIST");

        //Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
        //Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        //Map<String, Indicator> indicatorMap = new HashMap<>();
        //int category = Constants.PRICECOLUMN;
        //Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        List<String> keys = new ArrayList<>();
        /*
        for (String type : usedRecommenders.keySet()) {
            List<Recommend> list = usedRecommenders.get(type);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                //Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                ids.retainAll(aResult.keySet());
            }
        }
        */
        Map<String, Object[]> result = new HashMap<>();
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (Indicator indicator : indicators) {
                String indicatorName = indicator.indicatorName();
                    // TODO fix
                    Map<String, Double[][]> listMap = (Map<String, Double[][]>) cat.getIndicatorLocalResultMap().get(indicatorName).get(PipelineConstants.LIST);
                   //Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                    Double[][] aResult = listMap.get(id);
                    arrayResult = (Object[]) ArrayUtils.addAll(arrayResult, aResult[0]);
            }
            result.put(id, arrayResult);
        }

        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();
        double testRecommendQualBuySell = 0;
        Object[] retObj2 = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, conf.getAggregatorsIndicatorFuturedays(), conf.getTableDays(), conf.getAggregatorsIndicatorIntervaldays(), dateList, pairStockMap, pairDateMap, cat.getPeriod(), pairCatMap, categories, datareaders);
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj2[0];
        Map<double[], Double> mergedCatMap = new HashMap<>();
        for (int j = conf.getAggregatorsIndicatorFuturedays(); j < macdlen; j += conf.getAggregatorsIndicatorIntervaldays()) {
            //List<Double> macdLists[] = macdMinMax.get(j);
            //int newmacdidx = macdlen - 1 - j + conf.getTestRecommendFutureDays();
            //int curmacdidx = macdlen - 1 - j;
            Map<String, Double[]> momrsiMap = dayIndicatorMap.get(j);
           //System.out.println("j"+j);
            Map<double[], Double> retMap = getEvaluations(conf, j, retObj2);
            mergedCatMap.putAll(retMap);
            int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
            int curlistidx = listlen - 1 - j;
            //testRecommendQualBuySell += MACDRecommend.getQuality(buy, buysellMap, listMap, curlistidx, newlistidx);
        }
   
        
        // map from h/m to model to posnegcom map<model, results>
        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        log.info("Period " + title + " " + mapMap.keySet());
        if (conf.wantML()) {
            if (mergedCatMap.keySet().isEmpty()) {
                int jj = 0;
            }
            // TODO add a null check
            int arrayLength = mergedCatMap.keySet().iterator().next().length;
            for(double[] array : mergedCatMap.keySet()) {
                if (array.length != arrayLength) {
                    System.out.println("diff length " + arrayLength + " " + array.length);
                }
            }
            Map<Double, String> labelMapShort = createLabelMapShort();
            try {
                //List<MacdSubType> subTypes = wantedSubTypes();
                    for (MLClassifyDao mldao : mldaos) {
                            //String mapType = mapTypes.get(null);
                            //String mapName = mapType;
                            //System.out.println("mapget " + mapName + " " + mapMap.keySet());
                            Map<double[], Double> map = mergedCatMap;
                            for (MLClassifyModel model : mldao.getModels()) {          
                                System.out.println(map.values());
                            System.out.println("len1 " + arrayLength);
                            int i = 0;
                                    for (double[] val: map.keySet()) {
                                        System.out.println(Arrays.toString(val));
                                        if (i++ >= 5) break;
                                    }
                            Double testAccuracy = mldao.learntest(this, map, model, arrayLength, key, MYTITLE, 2, mapTime);  
                            probabilityMap.put(mldao.getName(), testAccuracy);
                            Map<Object, Long> countMap = map.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e), Collectors.counting()));                            
                            // make OO of this, create object
                            Object[] meta = new Object[6];
                            meta[0] = mldao.getName();
                            meta[1] = model.getName();
                            meta[2] = model.getReturnSize();
                            meta[3] = countMap;
                            meta[4] = testAccuracy;
                            resultMetaArray.add(meta);
                            ResultMeta resultMeta = new ResultMeta();
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setReturnSize(model.getReturnSize());
                            resultMeta.setLearnMap(countMap);
                            resultMeta.setTestAccuracy(testAccuracy);
                            getResultMetas().add(resultMeta);
                    }
                    }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            // calculate sections and do ML
            // a map from h/m + com/neg/sub to map<id, values>
            System.out.println(dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, double[]> indicatorMap3 = new HashMap<>();
            if (indicatorMap2 != null) {
            for (String id : indicatorMap2.keySet()) {
                indicatorMap3.put(id, ArraysUtil.convert(indicatorMap2.get(id)));
            }
            }
            // map from h/m + posnegcom to map<model, results>
           // List<MacdSubType> subTypes = wantedSubTypes();
            //for (MacdSubType subType : subTypes) {
                //Map<MLClassifyModel, Map<String, Double[]>> mapResult1 = new HashMap<>();
            int testCount = 0;   
            for (MLClassifyDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLClassifyModel model : mldao.getModels()) {
                        Map<String, double[]> map = indicatorMap3;
                            //log.info("map name " + mapName);
                            if (map == null) {
                                log.error("map null ");
                                continue;
                            } else {
                                log.info("keyset " + map.keySet());
                            }
                            System.out.println("len0 " + arrayLength);
                            int i = 0;
                                    for (double[] val: map.values()) {
                                        System.out.println(Arrays.toString(val));
                                        if (i++ >= 5) break;
                                    }
                            Map<String, Double[]> classifyResult = mldao.classify(this, map, model, arrayLength, key, MYTITLE, 2, labelMapShort, mapTime);
                            mapResult.put(model, classifyResult);
                            Map<String, Long> countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                            String counts = "classified ";
                            for (String label : countMap.keySet()) {
                                counts += label + " : " + countMap.get(label) + " ";
                            }
                            addEventRow(counts, "", "");  
                            //Object[] meta = new Object[3];
                            //meta[0] = mldao.getName();
                            //meta[1] = model.getEngineName();
                            Object[] meta = resultMetaArray.get(testCount++);
                            meta[5] = countMap;
                            ResultMeta resultMeta = getResultMetas().get(testCount - 1);
                            resultMeta.setClassifyMap(countMap);
                            //resultMeta.add(meta);
                        //mapResult1.put(model, mapResult2);
                           // mapResult.put("0", mapResult2);
                    }
                }
            //}
        }
        List<Map> maplist = new ArrayList<>();
        for (String id : listMap.keySet()) {
            //Double[] momentum = momMap.get(id);
            Object[] objs = objectMap.get(id);
            Object[] fields = new Object[fieldSize];
            //momMap.put(id, momentum);
            resultMap.put(id, fields);
            /*
            if (momentum == null) {
                System.out.println("zero mom for id " + id);
            }
            */
            int retindex = 0 ; //tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);
            
            // TODO make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                //int momidx = 6;
                Double[] type;
                //List<MacdSubType> subTypes2 = wantedSubTypes();
                //for (MacdSubType subType : subTypes2) {
                    //Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                    //System.out.println("mapget " + subType + " " + mapResult.keySet());
                    for (MLClassifyDao mldao : mldaos) {
                        for (MLClassifyModel model : mldao.getModels()) {
                            Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                            mapResult2.put(MYTITLE, mapResult.get(model));
                            //for (int mapTypeInt : getMapTypeList()) {
                                //String mapType = mapTypes.get(mapTypeInt);
                                //Map<String, Double[]> mapResult3 = mapResult2.get(mapType);
                                //String mapName = subType.getType() + mapType;
                                //System.out.println("fields " + fields.length + " " + retindex);
                            List<Integer> typeList = getTypeList();
                            Map<Integer, String> mapTypes = getMapTypes();
                            for (int mapTypeInt : typeList) {
                                String mapType = mapTypes.get(mapTypeInt);
                                Map<String, Double[]> resultMap1 = mapResult2.get(mapType);
                                Double[] aType = null;
                                if (resultMap1 != null) {
                                    aType = resultMap1.get(id);
                                } else {
                                    System.out.println("map null " + mapType);
                                }
                                fields[retindex++] = aType != null ? labelMapShort2.get(aType[0]) : null;
                                if (model.getReturnSize() > 1) {
                                fields[retindex++] = aType != null ? aType[1] : null;
                                } else {
                                    int jj =0;
                                }
             retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                              }
                            //}
                        }   
                    }
                //}
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
        // and others done with println
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            //List<MacdSubType> subTypes = wantedSubTypes();
           // for (MacdSubType subType : subTypes) {
                List<Integer> list = new ArrayList<>();
                //list.add(POSTYPE);
                //list.add(NEGTYPE);
                //for (Integer type : list) {
                    /*
                    String name = mapTypes.get(null);
                    String mapName = subType.getType() + name;
                    Map<double[], Double> myMap = mapMap.get(mapName);
                    if (myMap == null) {
                        log.error("map null " + mapName);
                        continue;
                    }
*/                
                    Map<Double, Long> countMap = mergedCatMap.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
                    String counts = "";
                    for (Double label : countMap.keySet()) {
                        counts += labelMapShort.get(label) + " : " + countMap.get(label) + " ";
                    }
                addEventRow(counts, "", "");
                //Object[] meta = new Object[1];
                //meta[0] = countMap;
                //resultMeta.add(meta);
                //}
            //}
        }
        if (conf.wantMLTimes()) {
            //Map<MLModel, Long> mapTime = new HashMap<>();
            for (MLClassifyModel model : mapTime.keySet()) {
                ResultItemTableRow row = new ResultItemTableRow();
                row.add("MLIndicator " + key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(mapTime.get(model));
                mlTimesTableRows.add(row);
            }
        }

    }

    private void getMlMappings(String name, Map<Double, String> labelMapShort, Map<String, double[]> commonMap,
            Map<String, double[]> posMap, Map<String, double[]> negMap, String id, double[] array, MInteger endOfArray,
            Double[] valueList) {
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
        printSignChange(name, id, newPos, newNeg, endOfArray.value, conf.getMACDDaysAfterZero(), labelMapShort);
        if (!newNeg.isEmpty() || !newPos.isEmpty()) {
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
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            commonMap.put(id, truncArray);
            if (!newNeg.isEmpty()) {
                negMap.put(id, truncArray); 
            }
            if (!newPos.isEmpty()) {
                posMap.put(id, truncArray);
            }
        }
    }
/*
    private void getMlMappings(String name, String subType, Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap,
            String id, double[] array, MInteger endOfArray,
            Double[] valueList) {
        Map<String, double[]> commonMap = mapGetter(mapIdMap, subType + CMNTYPESTR);
        Map<String, double[]> posMap = mapGetter(mapIdMap, subType + POSTYPESTR);
        Map<String, double[]> negMap = mapGetter(mapIdMap, subType + NEGTYPESTR);
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
        printSignChange(name, id, newPos, newNeg, endOfArray.value, conf.getMACDDaysAfterZero(), labelMapShort);
        if (!newNeg.isEmpty() || !newPos.isEmpty()) {
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
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            commonMap.put(id, truncArray);
            if (!newNeg.isEmpty()) {
                negMap.put(id, truncArray); 
            }
            if (!newPos.isEmpty()) {
                posMap.put(id, truncArray);
            }
        }
    }
*/
    private boolean anythingHere(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean anythingHere2(Map<String, Double[]> listMap2) {
        for (Double[] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getPosMap(Map<double[], Double> commonMap, Map<double[], Double> posMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer>[] map) {
        Map<Integer, Integer> pos = map[0];
        //System.out.println("Checking " + key + " " + id + " " + listsize + " " + histarr.length);
        if (list.length == 0) {
            //System.out.println("h " + Arrays.asList( histarr));
        }
        Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newPos.keySet()) {
            int end = newPos.get(start);
            String label = null;
            try {
                if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                    label = labelFN;
                    log.info(labelFN + ": " + id + " " + ControlService.getName(id) + " at " + end);
                    printme(label, end, list, array);
                } else {
                    label = labelTN;
                    log.info(labelTN + ": " + id + " " + ControlService.getName(id) + " at " + end);
                    printme(label, end, list, array);
                }
            } catch (Exception e) {
                log.error("myexcept " + pos + " : " + newPos + " " + start + " " + end + " " + list.length + " " + array.length, e);
            }
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double label2 = labelMap2.get(label);
            commonMap.put(truncArray, label2);
            posMap.put(truncArray, label2);
        }
    }

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    private void getNegMap(Map<double[], Double> commonMap, Map<double[], Double> negMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer>[] map) {
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newNeg.keySet()) {
            int end = newNeg.get(start);
            String label;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                label = labelTP;
                log.info("TruePositive" + ": " + id + " " + ControlService.getName(id) + " at " + end);
                printme(label, end, list, array);
            } else {
                label = labelFP;
                log.info(labelFP + ": " + id + " " + ControlService.getName(id) + " at " + end);
                printme(label, end, list, array);
            }
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double label2 = labelMap2.get(label);
            commonMap.put(truncArray, label2);
            negMap.put(truncArray, label2);
        }
    }

    private void getPosNegMap(Map<double[], Double> commonMap, Map<double[], Double> posnegMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String label, String labelopposite) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                textlabel = label;
            } else {
                textlabel = labelopposite;
            }
            log.info(textlabel + ": " + id + " " + ControlService.getName(id) + " at " + end);
            printme(textlabel, end, list, array);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double doublelabel = labelMap2.get(textlabel);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    private void getPosNegMap(Map<String, Map<double[], Double>> mapMap, String subType, String commonType, String posnegType , String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String label, String labelopposite) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
    //System.out.println("pnmap " + newPosNeg.keySet());
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                textlabel = label;
            } else {
                textlabel = labelopposite;
            }
            log.info(textlabel + ": " + id + " " + ControlService.getName(id) + " at " + end);
            printme(textlabel, end, list, array);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double doublelabel = labelMap2.get(textlabel);
            String commonMapName = subType + commonType;
            String posnegMapName = subType + posnegType;
            Map<double[], Double> commonMap = mapGetter(mapMap, commonMapName);
            Map<double[], Double> posnegMap = mapGetter(mapMap, posnegMapName);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    private <K, V> Map<K, V> mapGetter(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            //System.out.println("mapput " + key);
            mapMap.put(key, map);
        }
        return map;
    }

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = new Long(0);
        }
        val += add;
        map.put(key, val);
    }

    private void printme(String label, int end, Double[] values, double[] array) {
        String me1 = "";
        String me2 = "";
        for (int i = end - 3; i <= end + conf.getMACDDaysAfterZero(); i++) {
            me1 = me1 + values[i] + " ";
            me2 = me2 + array[i] + " ";
        }
        log.info("me1 " + me1);
        log.info("me2 " + me2);
    }

    public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }
    
    private void printSignChange(String txt, String id, Map<Integer, Integer> pos, Map<Integer, Integer> neg, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
        if (!pos.isEmpty()) {
            int posmaxind = Collections.max(pos.keySet());
            int posmax = pos.get(posmaxind);
            //System.out.println("truncls " + posmax + " " +  " " + listsize);
            if (posmax + 1 == listsize) {
                return;
            }
            if (posmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to negative since " + (listsize - posmax), ControlService.getName(id), id);
            }
        }
        if (!neg.isEmpty()) {
            int negmaxind = Collections.max(neg.keySet());
            int negmax = neg.get(negmaxind);
            if (negmax + 1 == listsize) {
                return;
            }
            if (negmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to positive since " + (listsize - negmax), ControlService.getName(id), id);
            }
        }
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //System.out.println("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, "Inc");
        labelMap1.put(2.0, "Dec");
        return labelMap1;
    }

    @Override
    public Object calculate(double[] array) {
        TaUtil tu = new TaUtil();
        Object[] objs = tu.getMomAndDeltaFull(array, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
        return objs;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsIndicatorML();
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
            //System.out.println("key " + key + " : " + periodDataMap.keySet());
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
        objs[retindex++] = title + Constants.WEBBR + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + Constants.WEBBR + "mom";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        retindex = getTitles(retindex, objs);
       // }
        //emptyField = new Double[size];
        log.info("fieldsizet " + retindex);
        return objs;
    }

    private int fieldSize() {
        int size = 0;
        //List<MacdSubType> subTypes = wantedSubTypes();
        //for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        //}
        emptyField = new Object[size];
        //log.info("fieldsizet " + size);
        return size;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] objs = new Object[fieldSize];
        int retindex = 0;
        //List<MacdSubType> subTypes = wantedSubTypes();
        //for (MacdSubType subType : subTypes) {
//            for (MLClassifyDao mldao : mldaos) {
  //              retindex = mldao.addTitles(objs, retindex, this, title, key, "grr");
    //        }
        Object[] fields = objs;
        if (resultMap != null) {
            fields = resultMap.get(stock.getId());
        }
        //}
        //log.info("fieldsizet " + retindex);
        row.addarr(fields);        
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        int retindex = 0;
        Object[] objs = new Object[fieldSize];
       // List<MacdSubType> subTypes = wantedSubTypes();
       // for (MacdSubType subType : subTypes) {
            retindex = getTitles(retindex, objs);
       // }
        System.out.println("retindex " + retindex);
        headrow.addarr(objs);
    }

    private int getTitles2(int retindex, Object[] objs) {
        for (MLClassifyDao mldao : mldaos) {
            retindex = mldao.addTitles(objs, retindex, this, title, key, MYTITLE);
        }
        return retindex;
    }
    
    private int getTitles(int retindex, Object[] objs) {
        // TODO make OO of this
        //List<MacdSubType> subTypes = wantedSubTypes();
        //for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    //for (int mapTypeInt : getMapTypeList()) {
                    //String mapType = mapTypes.get(mapTypeInt);
                    //String mapName = subType.getType() + mapType;
                    List<Integer> typeList = getTypeList();
                    Map<Integer, String> mapTypes = getMapTypes();
                    for (int mapTypeInt : typeList) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String val = "";
                        //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
                        String lr = "";
                        // TODO workaround
                        try {
                            val = "" + MLClassifyModel.roundme((Double) probabilityMap.get(mldao.getName()));
                            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
                        } catch (Exception e) {
                            log.error("Exception fix later, refactor", e);
                        }
                        objs[retindex++] = title + Constants.WEBBR +  model.getName() + mapType + " " + val;
                        if (model.getReturnSize() > 1) {
                        objs[retindex++] = title + Constants.WEBBR +  model.getName() + mapType + " prob ";
                        }
                        //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
                    }
                }
                //System.out.println("sizei "+retindex);
                //}
           // }
        }
        return retindex;
    }

    @Override
    public String getName() {
        return PipelineConstants.MLINDICATOR;
    }
    
}

