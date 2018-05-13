package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.IncDecItem;
import roart.model.MemoryItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.config.ConfigConstants;
import roart.config.IclijConfig;
import roart.config.IclijConfigConstants;
import roart.config.IclijXMLConfig;
import roart.config.MyMyConfig;
import roart.model.ResultMeta;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.ServiceUtil;
import roart.util.ServiceUtilConstants;
import roart.util.TimeUtil;

public class ComponentMLMACD extends Component {
    private Logger log = LoggerFactory.getLogger(ComponentMLMACD.class);
    @Override
    public void enable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);        
        conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
    }

    @Override
    public void disable(MyMyConfig conf) {
        conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);        
        conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
    }

    @Override
    public void handle(ControlService srv, MyMyConfig conf, Map<String, Map<String, Object>> resultMaps, List<Integer> positions, Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap, Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap, IclijConfig config, Map<String, Object> updateMap) {
        //if (true) return;
        //System.out.println(resultMaps.keySet());
        List<String> nns = getnns();
        setnns(conf, config, nns);
        if (config.wantEvolveML()) {
            Map<String, Object> anUpdateMap = srv.getEvolveML(true, new ArrayList<>(), PipelineConstants.MLMACD, conf);
            if (updateMap != null) {
                updateMap.putAll(anUpdateMap); 
            }
        }
        resultMaps = srv.getContent();
        Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLMACD);
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
                keys[0] = PipelineConstants.MLMACD;
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
                    keys = getRealKeys(keys, keyset);
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(FindProfitAction.TP) || tfpn.equals(FindProfitAction.FN)) {
                        increase = true;
                        IncDecItem incdec = mapAdder(buys, key, okConfMap.get(keys), okListMap.get(keys), nameMap, TimeUtil.convertDate(srv.conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(FindProfitAction.TN) || tfpn.equals(FindProfitAction.FP)) {
                        increase = false;
                        IncDecItem incdec = mapAdder(sells, key, okConfMap.get(keys), okListMap.get(keys), nameMap, TimeUtil.convertDate(srv.conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }

    }

    static void setnns(MyMyConfig conf, IclijConfig config, List<String> nns) {
        Map<String, String> map = config.getConv();
        for (String key : nns) {
            System.out.println(conf.getConfigValueMap().keySet());
            Object o = config.getValueOrDefault(key);
            boolean enable = (boolean) config.getValueOrDefault(key);
            String otherKey = map.get(key);
            conf.getConfigValueMap().put(otherKey, enable);
        }
    }

    static List<String> getnns() {
        List<String> nns = new ArrayList<>();
        nns.add(IclijConfigConstants.EVOLVEMLDNN);
        nns.add(IclijConfigConstants.EVOLVEMLDNNL);
        nns.add(IclijConfigConstants.EVOLVEMLL);
        nns.add(IclijConfigConstants.EVOLVEMLLR);
        nns.add(IclijConfigConstants.EVOLVEMLMCP);
        nns.add(IclijConfigConstants.EVOLVEMLOVR);
        return nns;
    }

    static Object[] getRealKeys(Object[] keys, Set<Object[]> keyset) {
        for (Object[] keyss : keyset) {
            if (keys[0].equals(keyss[0])) {
                if (keys[1] == null && keyss[1] == null) {
                    keys = keyss;
                    break;
                }
                if (keys[1].equals(keyss[1])) {
                    keys = keyss;
                    break;
                }
            }
        }
        return keys;
    }
    static IncDecItem mapAdder(Map<String, IncDecItem> map, String key, Double add, List<MemoryItem> memoryList, Map<String, String> nameMap, LocalDate date) {
        MemoryItem memory = memoryList.get(0);
        IncDecItem val = map.get(key);
        if (val == null) {
            val = new IncDecItem();
            val.setRecord(LocalDate.now());
            val.setDate(date);
            val.setId(key);
            val.setMarket(memory.getMarket());
            val.setDescription("");
            val.setName(nameMap.get(key));
            val.setScore(0.0);
            map.put(key, val);
        }
        val.setScore(val.getScore() + add);
        val.setDescription(val.getDescription() + memory.getSubcomponent() + ", ");
        return val;
    }

    @Override
    public Map<String, String> improve(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<Integer> positions,
            Map<String, IncDecItem> buys, Map<String, IncDecItem> sells, Map<Object[], Double> okConfMap,
            Map<Object[], List<MemoryItem>> okListMap, Map<String, String> nameMap) {
        log.info("Component not impl {}" + this.getClass().getName());
        return new HashMap<>();
    }

    public List<MemoryItem> calculateMLMACD(String market, int daysafterzero, LocalDate baseDate, LocalDate futureDate,
            String categoryTitle, Map<String, List<Object>> resultMap, List<List> resultMetaArray,
            Map<String, List<List<Double>>> categoryValueMap, List<ResultMeta> resultMeta, int offset, Integer usedsec, boolean doSave, boolean doPrint) throws Exception {
        List<MemoryItem> memoryList = new ArrayList<>();
        int resultIndex = 0;
        int count = 0;
        for (List meta : resultMetaArray) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.get(2);
            Double testaccuracy = (Double) meta.get(6);
            Map<String, List<Double>> offsetMap = (Map<String, List<Double>>) meta.get(8);
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
            long tpSize = 0;
            long fpSize = 0;
            long tnSize = 0;
            long fnSize = 0;
            double goodTPprob = 0;
            double goodFPprob = 0;
            double goodTNprob = 0;
            double goodFNprob = 0;
            int size = resultMap.values().iterator().next().size();
            for (String key : categoryValueMap.keySet()) {
                if (key.equals("VIX")) {
                    int jj = 0;
                }
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
                List<Double> off = offsetMap.get(key);
                if (off == null) {
                    log.error("The offset should not be null for {}", key);
                    continue;
                }
                int offsetZero = (int) Math.round(off.get(0));
                Double valFuture = mainList.get(mainList.size() - 1 - offset - offsetZero);
                Double valNow = mainList.get(mainList.size() - 1 - daysafterzero - offset - offsetZero);
                Double tfpnProb = null;
                if (returnSize > 1) {
                    tfpnProb = (Double) list.get(resultIndex + 1);
                }
                if (valFuture != null && valNow != null) {
                    //System.out.println("vals " + key + " " + valNow + " " + valFuture);
                    total++;
                    if (tfpn.equals(ServiceUtilConstants.TP)) {
                        tpSize++;
                        if (valFuture > valNow ) {
                            goodTP++;
                            if (returnSize > 1) {
                                goodTPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.FP)) {
                        fpSize++;
                        if (valFuture < valNow) {
                            goodFP++;
                            if (returnSize > 1) {
                                goodFPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.TN)) {
                        tnSize++;
                        if (valFuture < valNow) {
                            goodTN++;
                            if (returnSize > 1) {
                                goodTNprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.FN)) {
                        fnSize++;
                        if (valFuture > valNow) {
                            goodFN++;
                            if (returnSize > 1) {
                                goodFNprob += tfpnProb;
                            }
                        }
                    }
                }
            }
            //System.out.println("tot " + total + " " + goodTP + " " + goodFP + " " + goodTN + " " + goodFN);
            memory.setMarket(market);
            memory.setRecord(LocalDate.now());
            memory.setDate(baseDate);
            memory.setUsedsec(usedsec);
            memory.setFuturedays(daysafterzero);
            memory.setFuturedate(futureDate);
            memory.setComponent(PipelineConstants.MLMACD);
            memory.setCategory(categoryTitle);
            memory.setSubcomponent(meta.get(0) + ", " + meta.get(1) + ", " + meta.get(3) + ", " + meta.get(4));
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
                Double goodTPprobConf = goodTP != 0 ? goodTPprob / goodTP : null;
                Double goodFPprobConf = goodFP != 0 ? goodFPprob / goodFP : null;
                Double goodTNprobConf = goodTN != 0 ? goodTNprob / goodTN : null;
                Double goodFNprobConf = goodFN != 0 ? goodFNprob / goodFN : null;
                memory.setTpProbConf(goodTPprobConf);
                memory.setFpProbConf(goodFPprobConf);
                memory.setTnProbConf(goodTNprobConf);
                memory.setFnProbConf(goodFNprobConf);                
            }
            Integer tpClassOrig = null;
            Integer tnClassOrig = null;
            Integer fpClassOrig = null;
            Integer fnClassOrig = null;
            if (countMapClass != null) {
                tpClassOrig = countMapClass.containsKey(ServiceUtilConstants.TP) ? countMapClass.get(ServiceUtilConstants.TP) : 0;
                tnClassOrig = countMapClass.containsKey(ServiceUtilConstants.TN) ? countMapClass.get(ServiceUtilConstants.TN) : 0;
                fpClassOrig = countMapClass.containsKey(ServiceUtilConstants.FP) ? countMapClass.get(ServiceUtilConstants.FP) : 0;
                fnClassOrig = countMapClass.containsKey(ServiceUtilConstants.FN) ? countMapClass.get(ServiceUtilConstants.FN) : 0;
            }
            Integer tpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.TP) ? countMapLearn.get(ServiceUtilConstants.TP) : 0;
            Integer tnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.TN) ? countMapLearn.get(ServiceUtilConstants.TN) : 0;
            Integer fpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.FP) ? countMapLearn.get(ServiceUtilConstants.FP) : 0;
            Integer fnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.FN) ? countMapLearn.get(ServiceUtilConstants.FN) : 0;
            boolean doTP = countMapLearn.containsKey(ServiceUtilConstants.TP);
            boolean doFP = countMapLearn.containsKey(ServiceUtilConstants.FP);
            boolean doTN = countMapLearn.containsKey(ServiceUtilConstants.TN);
            boolean doFN = countMapLearn.containsKey(ServiceUtilConstants.FN);
            int keys = 0;
            if (doTP) {
                keys++;
            }
            if (doFP) {
                keys++;
            }
            if (doTN) {
                keys++;
            }
            if (doFN) {
                keys++;
            }
            Integer totalClass = null;
            if (countMapClass != null) {
                totalClass = tpClassOrig + tnClassOrig + fpClassOrig + fnClassOrig;
            }
            Integer totalSize = tpSizeOrig + tnSizeOrig + fpSizeOrig + fnSizeOrig;
            Double learnConfidence = null;
            if (countMapClass != null) {
                learnConfidence = keys != 0 && totalClass != 0 && totalSize != 0 ? (double) (
                        ( doTP ? Math.abs((double) tpClassOrig / totalClass - (double) tpSizeOrig / totalSize) : 0) +
                        ( doFP ? Math.abs((double) fpClassOrig / totalClass - (double) fpSizeOrig / totalSize) : 0) +
                        ( doTN ? Math.abs((double) tnClassOrig / totalClass - (double) tnSizeOrig / totalSize) : 0) +
                        ( doFN ? Math.abs((double) fnClassOrig / totalClass - (double) fnSizeOrig / totalSize) : 0)
                        ) / keys : null;
            }
            String info = null; 
            if (tpSizeOrig != null) {
                info = "Classified / learned: ";
                info += "TP " + tpClassOrig + " / " + tpSizeOrig + ", ";
                info += "TN " + tnClassOrig + " / " + tnSizeOrig + ", ";
                info += "FP " + fpClassOrig + " / " + fpSizeOrig + ", ";
                info += "FN " + fnClassOrig + " / " + fnSizeOrig + " ";
            }
            memory.setInfo(info);
            memory.setTpSize(tpSize);
            memory.setTnSize(tnSize);
            memory.setFpSize(fpSize);
            memory.setFnSize(fnSize);
            Double tpConf = (tpSize != 0 ? ((double) goodTP / tpSize) : null);
            Double tnConf = (tnSize != 0 ? ((double) goodTN / tnSize) : null);
            Double fpConf = (fpSize != 0 ? ((double) goodFP / fpSize) : null);
            Double fnConf = (fnSize != 0 ? ((double) goodFN / fnSize) : null);
            memory.setTpConf(tpConf);
            memory.setTnConf(tnConf);
            memory.setFpConf(fpConf);
            memory.setFnConf(fnConf);
            memory.setSize(total);
            Double conf = total != 0 ? ((double) goodTP + goodTN + goodFP + goodFN) / total : null;
            memory.setPositives(goodTP + goodTN + goodFP + goodFN);
            memory.setConfidence(conf);
            memory.setLearnConfidence(learnConfidence);
            memory.setPosition(count);
            if (doSave) {
                memory.save();
            }
            memoryList.add(memory);
            if (doPrint) {
            System.out.println(memory);
            }
            resultIndex += returnSize;
            count++;
        }
        return memoryList;
    }
}

