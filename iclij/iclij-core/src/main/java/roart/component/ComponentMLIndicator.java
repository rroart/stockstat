package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.IncDecItem;
import roart.model.MemoryItem;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.ServiceUtil;
import roart.util.ServiceUtilConstants;

public class ComponentMLIndicator extends Component {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final String INC = "Inc";
    private static final String DEC = "Dec";
    
    @Override
    public void enable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.MLINDICATOR, Boolean.TRUE);                
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.configValueMap.put(PipelineConstants.MLINDICATOR, Boolean.FALSE);        
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        // TODO Auto-generated method stub
        resultMaps = srv.getContent();
        Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLINDICATOR);
        //System.out.println("mlm " + mlMACDMaps.keySet());
        Integer category = (Integer) mlMACDMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlMACDMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());

        int resultIndex = 0;
        int count = 0;
        for (List meta : resultMetaArray) {
            int returnSize = (int) meta.get(2);

            if (positions.contains(count)) {
                Object[] keys = new Object[2];
                keys[0] = PipelineConstants.MLINDICATOR;
                keys[1] = count;
                for (String key : categoryValueMap.keySet()) {
                    List<List<Double>> resultList = categoryValueMap.get(key);
                    List<Double> mainList = resultList.get(0);
                    if (mainList == null) {
                        continue;
                    }
                    List<Object> list = resultMap.get(key);
                    if (list == null) {
                        continue;
                    }
                    String tfpn = (String) list.get(resultIndex);
                    if (tfpn == null) {
                        continue;
                    }
                    boolean increase = false;
                    //System.out.println(okConfMap.keySet());
                    Set<Object[]> keyset = okConfMap.keySet();
                    keys = ComponentMLMACD.getRealKeys(keys, keyset);
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(INC)) {
                        increase = true;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(buys, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(DEC)) {
                        increase = false;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(sells, key, okConfMap.get(keys), okListMap.get(keys), nameMap);
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }

    }
    @Override
    public Map<String, String> improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> badConfMap,
            Map<Object[], List<MemoryItem>> badListMap, Map<String, String> nameMap) {
        Map<String, String> retMap = new HashMap<>();
        List<String> permList = new ArrayList<>();
        String market = badListMap.values().iterator().next().get(0).getMarket();
        ControlService srv = new ControlService();
        srv.getConfig();            
        permList.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        permList.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        int size = permList.size();
        int bitsize = (1 << size) - 1;
        for (int i = 1; i < bitsize; i++) {
            String key = "";
            for (int j = 0; j < size; j++) {
                log.info("Doing {} {}", i, j);
                if ((i & (1 << j)) != 0) {
                    srv.conf.configValueMap.put(permList.get(j), Boolean.TRUE);
                    key = key + permList.get(j);
                } else {
                    srv.conf.configValueMap.put(permList.get(j), Boolean.FALSE);
                }
            }
            try {
                List<Double> newConfidenceList = new ArrayList<>();
                List<MemoryItem> memories = ServiceUtil.doMLIndicator(srv, market, 0, null, false, false);
                for(MemoryItem memory : memories) {
                    newConfidenceList.add(memory.getConfidence());
                }
                log.info("New confidences []", newConfidenceList);
                retMap.put(key, newConfidenceList.toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return retMap;
    }

    public List<MemoryItem> calculateMLindicator(String market, int futuredays, LocalDate baseDate, LocalDate futureDate, double threshold,
            Map<String, List<Object>> resultMap, int size0, Map<String, List<List<Double>>> categoryValueMap, List<ResultMeta> resultMeta, String categoryTitle, Integer usedsec, boolean doSave, boolean doPrint) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        int resultIndex = 0;
        int count = 0;
        for (ResultMeta meta : resultMeta) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.getReturnSize();
            Double testaccuracy = (Double) meta.getTestAccuracy();
            //Map<String, double[]> offsetMap = (Map<String, double[]>) meta.get(8);
            /*
            Map<String, Integer> countMapLearn = (Map<String, Integer>) meta.get(5);
            Map<String, Integer> countMapClass = (Map<String, Integer>) meta.get(7);
             */
            Map<String, Integer> countMapLearn = (Map<String, Integer>) resultMeta.get(count).getLearnMap();
            Map<String, Integer> countMapClass = (Map<String, Integer>) resultMeta.get(count).getClassifyMap();
            long total = 0;
            long goodTP = 0;
            long goodFP = 0;
            long goodTN = 0;
            long goodFN = 0;
            long incSize = 0;
            //long fpSize = 0;
            long decSize = 0;
            //long fnSize = 0;
            double goodTPprob = 0;
            double goodFPprob = 0;
            double goodTNprob = 0;
            double goodFNprob = 0;
            int size = resultMap.values().iterator().next().size();
            for (String key : categoryValueMap.keySet()) {
                List<List<Double>> resultList = categoryValueMap.get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList == null) {
                    continue;
                }
                //int offset = (int) offsetMap.get(key)[0];
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - futuredays);
                if (valFuture == null || valNow == null) {
                    continue;
                }
                boolean incThreshold = (valFuture / valNow - 1) >= threshold;
                List<Object> list = resultMap.get(key);
                if (list == null) {
                    continue;
                }
                String incdec = (String) list.get(resultIndex);
                if (incdec == null) {
                    continue;
                }
                Double incdecProb = null;
                if (returnSize > 1) {
                    incdecProb = (Double) list.get(resultIndex + 1);
                }
                total++;
                if (incdec.equals(ServiceUtilConstants.INC)) {
                    incSize++;
                    if (incThreshold) {
                        goodTP++;
                        if (returnSize > 1) {
                            goodTPprob += incdecProb;
                        }
                    } else {
                        goodFP++;
                        if (returnSize > 1) {
                            goodFPprob += (1 - incdecProb);                                    }
                    }
                }
                if (incdec.equals(ServiceUtilConstants.DEC)) {
                    decSize++;
                    if (!incThreshold) {
                        goodTN++;
                        if (returnSize > 1) {
                            goodTNprob += incdecProb;
                        }
                    } else {
                        goodFN++;
                        if (returnSize > 1) {
                            goodFNprob += (1 - incdecProb);
                        }
                    }
                }
            }
            //System.out.println("tot " + total + " " + goodTP + " " + goodFP + " " + goodTN + " " + goodFN);
            memory.setMarket(market);
            memory.setRecord(LocalDate.now());
            memory.setDate(baseDate);
            memory.setUsedsec(usedsec);
            memory.setFuturedays(futuredays);
            memory.setFuturedate(futureDate);
            memory.setComponent(PipelineConstants.MLINDICATOR);
            memory.setCategory(categoryTitle);
            memory.setSubcomponent(meta.getMlName() + ", " + meta.getModelName() + ", " + meta.getSubType() + ", " + meta.getSubSubType());
            memory.setTestaccuracy(testaccuracy);
            //memory.setPositives(goodInc);
            memory.setTp(goodTP);
            memory.setFp(goodFP);
            memory.setTn(goodTN);
            memory.setFn(goodFN);
            if (returnSize > 1) {
                memory.setTpProb(goodTPprob);
                memory.setFpProb(goodFPprob);
                memory.setTnProb(goodTNprob);
                memory.setFnProb(goodFNprob);      
                Double goodTPprobConf = goodTPprob / goodTP;
                Double goodFPprobConf = goodFPprob / goodFP;
                Double goodTNprobConf = goodTNprob / goodTN;
                Double goodFNprobConf = goodFNprob / goodFN;
                memory.setTpProbConf(goodTPprobConf);
                memory.setFpProbConf(goodFPprobConf);
                memory.setTnProbConf(goodTNprobConf);
                memory.setFnProbConf(goodFNprobConf);                
            }
            Integer tpClassOrig = countMapClass.containsKey(ServiceUtilConstants.INC) ? countMapClass.get(ServiceUtilConstants.INC) : 0;
            Integer tnClassOrig = countMapClass.containsKey(ServiceUtilConstants.DEC) ? countMapClass.get(ServiceUtilConstants.DEC) : 0;
            //Integer fpClassOrig = goodTP - ;
            //Integer fnClassOrig = countMapClass.containsKey(FN) ? countMapClass.get(FN) : 0;
            Integer tpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.INC) ? countMapLearn.get(ServiceUtilConstants.INC) : 0;
            Integer tnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.DEC) ? countMapLearn.get(ServiceUtilConstants.DEC) : 0;
            //Integer fpSizeOrig = countMapLearn.containsKey(FP) ? countMapLearn.get(FP) : 0;
            //Integer fnSizeOrig = countMapLearn.containsKey(FN) ? countMapLearn.get(FN) : 0;
            int keys = 2;
            Integer totalClass = tpClassOrig + tnClassOrig;
            Integer totalSize = tpSizeOrig + tnSizeOrig;
            Double learnConfidence = 0.0;
            learnConfidence = (double) (
                    ( true ? Math.abs((double) tpClassOrig / totalClass - (double) tpSizeOrig / totalSize) : 0) +
                    //( true ? Math.abs((double) fpClassOrig / totalClass - (double) fpSizeOrig / totalSize) : 0) +
                    ( true ? Math.abs((double) tnClassOrig / totalClass - (double) tnSizeOrig / totalSize) : 0)
                    //( true ? Math.abs((double) fnClassOrig / totalClass - (double) fnSizeOrig / totalSize) : 0)
                    ) / keys;
            String info = null; 
            if (tpSizeOrig != null) {
                info = "Classified / learned: ";
                info += ServiceUtilConstants.TP + " " + tpClassOrig + " / " + tpSizeOrig + ", ";
                info += ServiceUtilConstants.TN + " " + tnClassOrig + " / " + tnSizeOrig + ", ";
                info += ServiceUtilConstants.FP + " " + (tpSizeOrig - tpClassOrig) + " / " + tpSizeOrig + ", ";
                info += ServiceUtilConstants.FN + " " + (tnSizeOrig - tnClassOrig) + " / " + tnSizeOrig + " ";
            }
            memory.setInfo(info);
            memory.setTpSize(incSize);
            memory.setTnSize(decSize);
            memory.setFpSize(incSize);
            memory.setFnSize(decSize);
            Double tpConf = (incSize != 0 ? ((double) goodTP / incSize) : null);
            Double tnConf = (decSize != 0 ? ((double) goodTN / decSize) : null);
            Double fpConf = (incSize != 0 ? ((double) goodFP / incSize) : null);
            Double fnConf = (decSize != 0 ? ((double) goodFN / decSize) : null);
            memory.setTpConf(tpConf);
            memory.setTnConf(tnConf);
            memory.setFpConf(fpConf);
            memory.setFnConf(fnConf);
            memory.setSize(total);
            Double conf = ((double) goodTP + goodTN) / total;
            memory.setPositives(goodTP + goodTN);
            memory.setConfidence(conf);
            memory.setLearnConfidence(learnConfidence);
            memory.setPosition(count);
            if (doSave) {
                memory.save();
            }
            if (doPrint) {
            memoryList.add(memory);
            }
            System.out.println(memory);
            resultIndex += returnSize;
            count++;
        }
        return memoryList;
        /*
        int total = 0;
        int goodInc = 0;
        int goodDec = 0;
        for (String key : categoryValueMap.keySet()) {
            List<List<Double>> resultList = categoryValueMap.get(key);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() -1 - futuredays);
                List<Object> list = resultMap.get(key);
                if (list == null) {
                    continue;
                }
                for (int i = 0; i < size; i++) {
                    String incDec = (String) list.get(i);
                    if (incDec == null) {
                        continue;
                    }
                    if (valFuture != null && valNow != null) {
                        double change = valFuture / valNow;
                        total++;
                        if (change >= threshold && incDec.equals("Inc")) {
                            goodInc++;
                        }
                        if (change < threshold && incDec.equals("Dec")) {
                            goodDec++;
                        }
                    }
                }
            }
        }
        */
        //System.out.println("tot " + total + " " + goodInc + " " + goodDec);
    }
}

