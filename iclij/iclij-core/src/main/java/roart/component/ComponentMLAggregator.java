package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.action.MarketAction;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLAggregatorData;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;
import roart.util.ServiceUtilConstants;

public abstract class ComponentMLAggregator extends ComponentML {

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters) {
        MLAggregatorData param = new MLAggregatorData(componentparam);

        int daysafterzero = getDaysAfterLimit(componentparam);
        param.setFuturedays(daysafterzero);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        return param;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        ConfigMapGene gene = new ConfigMapGene(confList, param.getService().conf);
        ConfigMapChromosome chromosome = getNewChromosome(action, market, profitdata, positions, buy, param, subcomponent, parameters, gene, mlTests);
        loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        return improve(action, param, chromosome, subcomponent);
    }

    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, List<Integer> positions, Boolean above, List<MLMetricsItem> mlTests, Parameters parameters) {
        ComponentMLData param = (ComponentMLData) componentparam;
        if (positions == null) {
            return;
        }
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        System.out.println("c " + aResultMap.keySet());
        System.out.println("a " + resultMap.keySet());
        System.out.println("b " + param.getCategoryValueMap().keySet());
        System.out.println("d " + profitdata.getInputdata().getConfMap().keySet());
        for (Pair<String, Integer> key : profitdata.getInputdata().getConfMap().keySet()) {
            System.out.println("e " + key);
        }
        int resultIndex = 0;
        int count = 0;
        for (List meta : param.getResultMetaArray()) {
            int returnSize = (int) meta.get(ResultMetaConstants.RETURNSIZE);

            boolean emptyMeta = meta.get(ResultMetaConstants.MLNAME) == null;
            
            if (emptyMeta) {
                resultIndex += returnSize;
                count++;
                continue;
            }
            if (positions == null) {
                int jj = 0;
            }
            MLMetricsItem mltest = search(mlTests, meta);
            if (mltest != null || (mlTests == null && (positions == null || positions.contains(count)))) {
                Double score = mltest.getTestAccuracy();
                Pair keyPair = new ImmutablePair(getPipeline(), count);
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
                    //Set<Pair<String, Integer>> keyset = profitdata.getInputdata().getConfMap().keySet();
                    //keyPair = getRealKeys(keyPair, keyset);
                    //System.out.println(okListMap.keySet());
                    if (above == null || above == true) {
                    if (tfpn.equals(Constants.TP) || tfpn.equals(Constants.FN)) {
                        increase = true;
                        //IncDecItem incdec = mapAdder(profitdata.getBuys(), key, profitdata.getInputdata().getAboveConfMap().get(keyPair), profitdata.getInputdata().getAboveListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        IncDecItem incdec = mapAdder(profitdata.getBuys(), key, score, profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()), param.getInput().getMarket(), mltest.getSubcomponent(), mltest.getLocalcomponent(), JsonUtil.convert(parameters));
                        incdec.setIncrease(increase);
                    }
                    }
                    if (above == null || above == false) {
                    if (tfpn.equals(Constants.TN) || tfpn.equals(Constants.FP)) {
                        increase = false;
                        //IncDecItem incdec = mapAdder(profitdata.getSells(), key, profitdata.getInputdata().getBelowConfMap().get(keyPair), profitdata.getInputdata().getBelowListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        IncDecItem incdec = mapAdder(profitdata.getSells(), key, score, profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()), param.getInput().getMarket(), mltest.getSubcomponent(), mltest.getLocalcomponent(), JsonUtil.convert(parameters));
                        incdec.setIncrease(increase);
                    }
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData componentparam, Parameters parameters) throws Exception {
        ComponentMLData param = (ComponentMLData) componentparam;
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        List<MemoryItem> memoryList = new ArrayList<>();
        int resultIndex = 0;
        int newResultIndex = 0;
        for (int count = 0; count < param.getResultMetaArray().size(); count++) {
            List meta = param.getResultMetaArray().get(count);
            resultIndex = newResultIndex;
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.get(ResultMetaConstants.RETURNSIZE);
            newResultIndex += returnSize;
            if (meta.get(ResultMetaConstants.MLNAME) == null) {
                continue;
            }
            
            Double testaccuracy = (Double) meta.get(ResultMetaConstants.TESTACCURACY);
            Double testloss = (Double) meta.get(ResultMetaConstants.LOSS);
            Map<String, List<Double>> offsetMap = (Map<String, List<Double>>) meta.get(ResultMetaConstants.OFFSETMAP);
            /*
            Map<String, Integer> countMapLearn = (Map<String, Integer>) meta.get(5);
            Map<String, Integer> countMapClass = (Map<String, Integer>) meta.get(7);
             */
            Map<String, Integer> countMapLearn = (Map<String, Integer>) param.getResultMeta().get(count).getLearnMap();
            Map<String, Integer> countMapClass = (Map<String, Integer>) param.getResultMeta().get(count).getClassifyMap();
            long total = 0;
	    // all the ones correctly predicted
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
            Map<Double, String> labelMap = createLabelMapShort();
            Map<String, List<Double>> classifyMap = (Map<String, List<Double>>) meta.get(ResultMetaConstants.CLASSIFYMAP);
            //int size = param.getResultMap().values().iterator().next().size();
            if (classifyMap == null) {
                int jj = 0;
                continue;
            }
            for (Entry<String, List<Double>> entry : classifyMap.entrySet()) {
                String key = entry.getKey();
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
                tfpn = labelMap.get(entry.getValue().get(0));
                if (tfpn == null) {
                    continue;
                }
                if (offsetMap == null) {
                    log.info("Offset map null, skipping rest");
                    continue;
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
                    if (tfpnProb == null) {
                        log.error("Prob null");
                    }
                }
                if (valFuture != null && valNow != null) {
                    //System.out.println("vals " + key + " " + valNow + " " + valFuture);
                    boolean aboveThreshold = (valFuture / valNow) >= (Double) meta.get(ResultMetaConstants.THRESHOLD);
                    total++;
                    if (tfpn.equals(ServiceUtilConstants.TP)) {
                        tpSize++;
                        if (aboveThreshold) {
                            goodTP++;
                            if (returnSize > 1 && tfpnProb != null) {
                                goodTPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.FP)) {
                        fpSize++;
                        if (!aboveThreshold) {
                            goodFP++;
                            if (returnSize > 1 && tfpnProb != null) {
                                goodFPprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.TN)) {
                        tnSize++;
                        if (!aboveThreshold) {
                            goodTN++;
                            if (returnSize > 1 && tfpnProb != null) {
                                goodTNprob += tfpnProb;
                            }
                        }
                    }
                    if (tfpn.equals(ServiceUtilConstants.FN)) {
                        fnSize++;
                        if (aboveThreshold) {
                            goodFN++;
                            if (returnSize > 1 && tfpnProb != null) {
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
            memory.setSubcomponent(meta.get(ResultMetaConstants.MLNAME) + " " + meta.get(ResultMetaConstants.MODELNAME));
            memory.setDescription(getShort((String) meta.get(ResultMetaConstants.MLNAME)) + withComma(getShort((String) meta.get(ResultMetaConstants.MODELNAME))) + withComma(meta.get(ResultMetaConstants.SUBTYPE)) + withComma(meta.get(ResultMetaConstants.SUBSUBTYPE)));
            memory.setTestaccuracy(testaccuracy);
            memory.setTestloss(testloss);
            memory.setParameters(JsonUtil.convert(parameters));
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
        }
        return memoryList;
    }
    
    protected abstract ConfigMapChromosome getNewChromosome(MarketAction action, Market market, ProfitData profitdata,
            List<Integer> positions, Boolean buy, ComponentData param, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests);

    protected abstract int getDaysAfterLimit(ComponentData componentparam);
    
    static Object[] getRealKeys(Object[] keys, Set<Object[]> keyset) {
        for (Object[] keyss : keyset) {
            if (keys[0].equals(keyss[0])) {
                if ((keys[1] == null && keyss[1] == null) || (keys[1].equals(keyss[1]))) {
                    if (keys[2] == null && keyss[2] == null) {
                        keys = keyss;
                        break;
                    }
                    if (keys[2].equals(keyss[2])) {
                        keys = keyss;
                        break;
                    }
                }
            }
        }
        return keys;
    }
    
    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, Constants.TP);
        labelMap1.put(2.0, Constants.FP);
        labelMap1.put(3.0, Constants.TN);
        labelMap1.put(4.0, Constants.FN);
        return labelMap1;
    }

}
