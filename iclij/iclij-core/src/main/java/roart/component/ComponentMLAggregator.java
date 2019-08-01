package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.action.FindProfitAction;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLAggregatorData;
import roart.component.model.MLCCIData;
import roart.config.Market;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtilConstants;

public abstract class ComponentMLAggregator extends ComponentML {

    @Override
    public ComponentData handle(Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) {
        MLAggregatorData param = new MLAggregatorData(componentparam);

        int daysafterzero = getDaysAfterLimit(componentparam);
        param.setFuturedays(daysafterzero);

        handle2(market, param, profitdata, positions, evolve, aMap);
        Map resultMaps = param.getResultMap();
        handleMLMeta(param, resultMaps);
        return param;
    }

    @Override
    public ComponentData improve(ComponentData componentparam, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy) {
        ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        ConfigMapChromosome chromosome = getNewChromosome(market, profitdata, positions, buy, param, confList);
        loadme(param, chromosome, market, confList, buy);
        return improve(param, chromosome);
    }

    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, List<Integer> positions) {
        ComponentMLData param = (ComponentMLData) componentparam;
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        System.out.println("c " + aResultMap.keySet());
        System.out.println("a " + resultMap.keySet());
        System.out.println("b " + param.getCategoryValueMap().keySet());
        System.out.println("d " + profitdata.getInputdata().getConfMap().keySet());
        for (Object[] key : profitdata.getInputdata().getConfMap().keySet()) {
            System.out.println("e " + ((String)key[0]) + " " + ((int)key[1]));
        }
        int resultIndex = 0;
        int count = 0;
        for (List meta : param.getResultMetaArray()) {
            int returnSize = (int) meta.get(2);

            if (positions == null) {
                int jj = 0;
            }
            if (positions == null || positions.contains(count)) {
                Object[] keys = new Object[2];
                keys[0] = getPipeline();
                keys[1] = count;
                for (String key : param.getCategoryValueMap().keySet()) {
                    List<List<Double>> resultList = param.getCategoryValueMap().get(key);
                    List<Double> mainList = resultList.get(0);
                    if (mainList == null) {
                        continue;
                    }
                    List<Object> list = (List<Object>) aResultMap.get(key);
                    if (list == null) {
                        continue;
                    }
                    String tfpn = (String) list.get(resultIndex);
                    if (tfpn == null) {
                        continue;
                    }
                    boolean increase = false;
                    //System.out.println(okConfMap.keySet());
                    Set<Object[]> keyset = profitdata.getInputdata().getConfMap().keySet();
                    keys = getRealKeys(keys, keyset);
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(FindProfitAction.TP) || tfpn.equals(FindProfitAction.FN)) {
                        increase = true;
                        IncDecItem incdec = mapAdder(profitdata.getBuys(), key, profitdata.getInputdata().getConfMap().get(keys), profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(FindProfitAction.TN) || tfpn.equals(FindProfitAction.FP)) {
                        increase = false;
                        IncDecItem incdec = mapAdder(profitdata.getSells(), key, profitdata.getInputdata().getConfMap().get(keys), profitdata.getInputdata().getListMap().get(keys), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData componentparam) throws Exception {
        ComponentMLData param = (ComponentMLData) componentparam;
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        List<MemoryItem> memoryList = new ArrayList<>();
        int resultIndex = 0;
        int count = 0;
        for (List meta : param.getResultMetaArray()) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.get(2);
            Double testaccuracy = (Double) meta.get(6);
            Map<String, List<Double>> offsetMap = (Map<String, List<Double>>) meta.get(8);
            /*
            Map<String, Integer> countMapLearn = (Map<String, Integer>) meta.get(5);
            Map<String, Integer> countMapClass = (Map<String, Integer>) meta.get(7);
             */
            Map<String, Integer> countMapLearn = (Map<String, Integer>) param.getResultMeta().get(count).getLearnMap();
            Map<String, Integer> countMapClass = (Map<String, Integer>) param.getResultMeta().get(count).getClassifyMap();
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
            //int size = param.getResultMap().values().iterator().next().size();
            for (String key : param.getCategoryValueMap().keySet()) {
                if (key.equals("VIX")) {
                    int jj = 0;
                }
                List<List<Double>> resultList = param.getCategoryValueMap().get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList == null) {
                    continue;
                }
                List<Object> list = (List<Object>) aResultMap.get(key);
                if (list == null) {
                    continue;
                }
                if (list.get(resultIndex) != null && !(list.get(resultIndex) instanceof String)) {
                    int jj = 0;
                }
                String tfpn = (String) list.get(resultIndex);
                if (tfpn == null) {
                    continue;
                }
                if (offsetMap == null) {
                    int jj = 0;
                }
                List<Double> off = offsetMap.get(key);
                if (off == null) {
                    log.error("The offset should not be null for {}", key);
                    continue;
                }
                int offsetZero = (int) Math.round(off.get(0));
                Double valFuture = mainList.get(mainList.size() - 1 - param.getLoopoffset() - offsetZero);
                Double valNow = mainList.get(mainList.size() - 1 - param.getFuturedays() - param.getLoopoffset() - offsetZero);
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
            memory.setMarket(param.getMarket());
            memory.setRecord(LocalDate.now());
            memory.setDate(param.getBaseDate());
            memory.setUsedsec(param.getUsedsec());
            memory.setFuturedays(param.getFuturedays());
            memory.setFuturedate(param.getFutureDate());
            memory.setComponent(getPipeline());
            memory.setCategory(param.getCategoryTitle());
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
            int keys = 0;
            Integer tpSizeOrig = null;
            Integer tnSizeOrig = null;
            Integer fpSizeOrig = null;
            Integer fnSizeOrig = null;
            boolean doTP = false;
            boolean doFP = false;
            boolean doTN = false;
            boolean doFN = false;
            if (countMapLearn != null) {
                tpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.TP) ? countMapLearn.get(ServiceUtilConstants.TP) : 0;
                tnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.TN) ? countMapLearn.get(ServiceUtilConstants.TN) : 0;
                fpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.FP) ? countMapLearn.get(ServiceUtilConstants.FP) : 0;
                fnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.FN) ? countMapLearn.get(ServiceUtilConstants.FN) : 0;
                doTP = countMapLearn.containsKey(ServiceUtilConstants.TP);
                doFP = countMapLearn.containsKey(ServiceUtilConstants.FP);
                doTN = countMapLearn.containsKey(ServiceUtilConstants.TN);
                doFN = countMapLearn.containsKey(ServiceUtilConstants.FN);
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
            }
            Integer totalClass = null;
            if (countMapClass != null) {
                totalClass = tpClassOrig + tnClassOrig + fpClassOrig + fnClassOrig;
            }
            Integer totalSize = null;
            if (countMapLearn != null) {
                totalSize = tpSizeOrig + tnSizeOrig + fpSizeOrig + fnSizeOrig;
            }
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
            if (param.isDoSave()) {
                memory.save();
            }
            memoryList.add(memory);
            if (param.isDoPrint()) {
                System.out.println(memory);
            }
            resultIndex += returnSize;
            count++;
        }
        return memoryList;
    }
    
    protected abstract ConfigMapChromosome getNewChromosome(Market market, ProfitData profitdata, List<Integer> positions,
            Boolean buy, ComponentData param, List<String> confList);

    protected abstract int getDaysAfterLimit(ComponentData componentparam);
    
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
        if (memoryList == null) {
            int jj = 0;
            System.out.println("Key " + key);
            if (map != null) {
                System.out.println("Keys" + map.keySet());
            }
        }
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
        String component = memory.getComponent();
        component = component != null ? component.substring(0, 3) : component;
        val.setDescription(val.getDescription() + component + " " + memory.getSubcomponent() + ", ");
        return val;
    }

}