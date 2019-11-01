package roart.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.MLConfig;
import roart.iclij.config.MLConfigs;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentInput;
import roart.component.model.MLIndicatorData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.chromosome.impl.MLIndicatorChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtilConstants;

public abstract class ComponentMLIndicator extends ComponentML {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String INC = "Inc";
    private static final String DEC = "Dec";

    @Override
    public void enable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.TRUE);        
        valueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);        
        valueMap.put(PipelineConstants.MLINDICATOR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);        
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSATRDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSCCIDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDMACDDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDSIGNALDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSRSIDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.TRUE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.TRUE);                
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);        
        valueMap.put(PipelineConstants.MLINDICATOR, Boolean.FALSE);        
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
        valueMap.put(ConfigConstants.INDICATORSATR, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCH, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSI, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSATRDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSCCIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDMACDDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSMACDMACDSIGNALDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSRSIDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHSTOCHDELTA, Boolean.FALSE);                
        valueMap.put(ConfigConstants.INDICATORSSTOCHRSIDELTA, Boolean.FALSE);                
    }

    public static MLConfigs getDisableLSTM() {
        EvolveMLConfig config = new EvolveMLConfig();
        config.setEnable(false);
        config.setEvolve(false);
        MLConfigs configs = new MLConfigs();
        configs.getTensorflow().setPredictorlstm(config);
        return configs;
    }

    private void handle2not(Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions) {

        MLIndicatorData param = new MLIndicatorData(componentparam);

        List<String> nns = ComponentMLMACD.getnns();
        ComponentMLMACD.setnns(param.getService().conf, param.getInput().getConfig(), nns);
        String localMl = param.getInput().getConfig().getFindProfitMLIndicatorMLConfig();
        String ml = param.getInput().getConfig().getEvolveMLMLConfig();
        MLConfigs marketMlConfig = market.getMlconfig();
        MLConfigs mlConfig = JsonUtil.convert(ml, MLConfigs.class);
        MLConfigs localMLConfig = JsonUtil.convert(localMl, MLConfigs.class);
        MLConfigs disableLSTM = getDisableLSTM();
        mlConfig.merge(localMLConfig);
        mlConfig.merge(marketMlConfig);
        mlConfig.merge(disableLSTM);
        Map<String, EvolveMLConfig> mlConfigMap = mlConfig.getAll();
        if (param.getInput().getConfig().wantEvolveML()) {
            //ComponentMLMACD.setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, true);
            Map<String, Object> anUpdateMap = (Map<String, Object>) param.getService().getEvolveML(true, new ArrayList<>(), PipelineConstants.MLINDICATOR, param.getService().conf, null, null);
            if (param.getInput().getValuemap() != null) {
                param.getInput().getValuemap().putAll(anUpdateMap); 
            }
        }
        //ComponentMLMACD.setnns(param.getService().conf, param.getInput().getConfig(), mlConfigMap, false);
        Map mlIndicatorMaps = (Map) param.getResultMap(PipelineConstants.MLINDICATOR, new HashMap<>());
        //Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLMACD);
        param.setCategory(mlIndicatorMaps);
        //resultMaps = srv.getContent();
        //Map mlMACDMaps = (Map) resultMaps.get(PipelineConstants.MLINDICATOR);
        //System.out.println("mlm " + mlMACDMaps.keySet());
        //Integer category = (Integer) mlMACDMaps.get(PipelineConstants.CATEGORY);
        //String categoryTitle = (String) mlMACDMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlIndicatorMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlIndicatorMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlIndicatorMaps.get(PipelineConstants.RESULTMETAARRAY);
        param.setResultMetaArray(resultMetaArray);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        //List<Object> objectList = m;
        //List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //param.setResultMeta(resultMeta);
        param.getAndSetCategoryValueMap();
        //Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());

        //calculateIncDec(positions, profitdata, resultMap, param);

    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData componentparam, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent) { //, String pipeline, String localMl, MLConfigs overrideLSTM, boolean evolve) {

        MLIndicatorData param = new MLIndicatorData(componentparam);

        int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(futuredays);
        double threshold = param.getService().conf.getAggregatorsIndicatorThreshold();
        param.setThreshold(threshold);

        handle2(action, market, param, profitdata, positions, evolve, aMap, subcomponent);
        //Map resultMaps = param.getResultMap();
        //handleMLMeta(param, resultMaps);
        //Map<String, Object> resultMap = param.getResultMap();
        return param;
        //calculateIncDec(positions, profitdata, resultMap, param);
    }

    @Override
    public void calculateIncDec(ComponentData componentparam, ProfitData profitdata, List<Integer> positions) {
        MLIndicatorData param = (MLIndicatorData) componentparam;
        if (positions == null) {
            return;
        }
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        int resultIndex = 0;
        int count = 0;
        if (param.getResultMetaArray() == null) {
            int jj = 0;
        }
        for (List meta : param.getResultMetaArray()) {
            int returnSize = (int) meta.get(2);

            if (positions == null) {
                int jj = 0;
            }
            if (positions == null || positions.contains(count)) {
                Pair keyPair = new ImmutablePair(PipelineConstants.MLINDICATOR, count);
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
                    Set<Pair<String, Integer>> keyset = profitdata.getInputdata().getConfMap().keySet();
                    //keyPair = ComponentMLAggregator.getRealKeys(keyPair, keyset);
                    //System.out.println(okListMap.keySet());
                    if (tfpn.equals(INC)) {
                        increase = true;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getBuys(), key, profitdata.getInputdata().getConfMap().get(keyPair), profitdata.getInputdata().getListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                    if (tfpn.equals(DEC)) {
                        increase = false;
                        IncDecItem incdec = ComponentMLMACD.mapAdder(profitdata.getSells(), key, profitdata.getInputdata().getConfMap().get(keyPair), profitdata.getInputdata().getListMap().get(keyPair), profitdata.getInputdata().getNameMap(), TimeUtil.convertDate(param.getService().conf.getdate()));
                        incdec.setIncrease(increase);
                    }
                }                        
            }

            resultIndex += returnSize;
            count++;
        }
    }

    private boolean anythingHere(Map<String, List<List<Double>>> listMap2, int size) {
        for (List<List<Double>> array : listMap2.values()) {
            if (size == 3 && size != array.get(0).size()) {
                return false;
            }
            for (int i = 0; i < array.get(0).size(); i++) {
                if (array.get(0).get(i) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy, String subcomponent) {
        ComponentData param = new ComponentData(componentparam);
        List<String> confList = getConfList();
        Map<String, List<List<Double>>> listMap = param.getCategoryValueMap();
        boolean gotThree = anythingHere(listMap, 3);
        if (gotThree) {
            confList.addAll(getThreeConfList());
        }
        ConfigMapChromosome chromosome = new MLIndicatorChromosome(action, confList, param, profitdata, market, positions, PipelineConstants.MLINDICATOR, buy, subcomponent);
        loadme(param, chromosome, market, confList, buy, subcomponent);
        return improve(action, param, chromosome, subcomponent);
    }

    public Map<String, String> improveNot(ComponentData param, Market market, MyMyConfig conf, ProfitData profitdata, List<Integer> positions) {
        Map<String, String> retMap = new HashMap<>();
        List<String> permList = new ArrayList<>();
        String marketName = profitdata.getInputdata().getListMap().values().iterator().next().get(0).getMarket();
        //ControlService srv = new ControlService();
        //srv.getConfig();            
        permList.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        permList.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        Map<String, Object> confMap = new HashMap<>();
        int size = permList.size();
        int bitsize = (1 << size) - 1;
        for (int i = 1; i < bitsize; i++) {
            String key = "";
            for (int j = 0; j < size; j++) {
                log.info("Doing {} {}", i, j);
                if ((i & (1 << j)) != 0) {
                    confMap.put(permList.get(j), Boolean.TRUE);
                    key = key + permList.get(j);
                } else {
                    confMap.put(permList.get(j), Boolean.FALSE);
                }
            }
            try {
                List<Double> newConfidenceList = new ArrayList<>();
                List<MemoryItem> memories = new MLService().doMLIndicator(new ComponentInput(marketName, null, null, false, false), confMap);
                {

                }
                for(MemoryItem memory : memories) {
                    newConfidenceList.add(memory.getConfidence());
                }
                log.info("New confidences {}", newConfidenceList);
                retMap.put(key, newConfidenceList.toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return retMap;
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData componentparam) throws Exception {
        MLIndicatorData param = (MLIndicatorData) componentparam;
        List<MemoryItem> memoryList = new ArrayList<>();
        Map<String, Object> resultMap = param.getResultMap();
        Map<String, List<Object>> aResultMap =  (Map<String, List<Object>>) resultMap.get(PipelineConstants.RESULT);
        int resultIndex = 0;
        int count = 0;
        for (ResultMeta meta : param.getResultMeta()) {
            MemoryItem memory = new MemoryItem();
            int returnSize = (int) meta.getReturnSize();
            Double testaccuracy = (Double) meta.getTestAccuracy();
            //Map<String, double[]> offsetMap = (Map<String, double[]>) meta.get(8);
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
            long incSize = 0;
            //long fpSize = 0;
            long decSize = 0;
            //long fnSize = 0;
            double goodTPprob = 0;
            double goodFPprob = 0;
            double goodTNprob = 0;
            double goodFNprob = 0;
            //int size = param.getResultMap().values().iterator().next().size();
            for (String key : param.getCategoryValueMap().keySet()) {
                List<List<Double>> resultList = param.getCategoryValueMap().get(key);
                List<Double> mainList = resultList.get(0);
                if (mainList == null) {
                    continue;
                }
                //int offset = (int) offsetMap.get(key)[0];
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - param.getFuturedays());
                if (valFuture == null || valNow == null) {
                    continue;
                }
                boolean incThreshold = (valFuture / valNow - 1) >= param.getThreshold();
                List<Object> list = (List<Object>) aResultMap.get(key);
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
                    if (incdecProb == null) {
                        log.error("Prob null");
                    }
                }
                total++;
                if (incdec.equals(ServiceUtilConstants.INC)) {
                    incSize++;
                    if (incThreshold) {
                        goodTP++;
                        if (returnSize > 1 && incdecProb != null) {
                            goodTPprob += incdecProb;
                        }
                    } else {
                        goodFP++;
                        if (returnSize > 1 && incdecProb != null) {
                            goodFPprob += (1 - incdecProb);                                    }
                    }
                }
                if (incdec.equals(ServiceUtilConstants.DEC)) {
                    decSize++;
                    if (!incThreshold) {
                        goodTN++;
                        if (returnSize > 1 && incdecProb != null) {
                            goodTNprob += incdecProb;
                        }
                    } else {
                        goodFN++;
                        if (returnSize > 1 && incdecProb != null) {
                            goodFNprob += (1 - incdecProb);
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
            memory.setComponent(PipelineConstants.MLINDICATOR);
            memory.setCategory(param.getCategoryTitle());
            memory.setSubcomponent(meta.getMlName() + " " + meta.getModelName());
            memory.setDescription(getShort(meta.getMlName()) + withComma(getShort(meta.getModelName())) + withComma(meta.getSubType()) + withComma(meta.getSubSubType()));
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
            Integer tpClassOrig = 0;
            Integer tnClassOrig = 0;
            if (countMapClass != null) {
                tpClassOrig = countMapClass.containsKey(ServiceUtilConstants.INC) ? countMapClass.get(ServiceUtilConstants.INC) : 0;
                tnClassOrig = countMapClass.containsKey(ServiceUtilConstants.DEC) ? countMapClass.get(ServiceUtilConstants.DEC) : 0;
            }
            //Integer fpClassOrig = goodTP - ;
            //Integer fnClassOrig = countMapClass.containsKey(FN) ? countMapClass.get(FN) : 0;
            Integer tpSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.INC) ? countMapLearn.get(ServiceUtilConstants.INC) : 0;
            Integer tnSizeOrig = countMapLearn.containsKey(ServiceUtilConstants.DEC) ? countMapLearn.get(ServiceUtilConstants.DEC) : 0;
            //Integer fpSizeOrig = countMapLearn.containsKey(FP) ? countMapLearn.get(FP) : 0;
            //Integer fnSizeOrig = countMapLearn.containsKey(FN) ? countMapLearn.get(FN) : 0;
            int keys = 2;
            Integer totalClass = tpClassOrig + tnClassOrig;
            Integer totalSize = tpSizeOrig + tnSizeOrig;
            Double learnConfidence = null;
            if (countMapClass != null) {
                learnConfidence = (double) (
                        ( true ? Math.abs((double) tpClassOrig / totalClass - (double) tpSizeOrig / totalSize) : 0) +
                        //( true ? Math.abs((double) fpClassOrig / totalClass - (double) fpSizeOrig / totalSize) : 0) +
                        ( true ? Math.abs((double) tnClassOrig / totalClass - (double) tnSizeOrig / totalSize) : 0)
                        //( true ? Math.abs((double) fnClassOrig / totalClass - (double) fnSizeOrig / totalSize) : 0)
                        ) / keys;
            }
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
            if (param.isDoSave()) {
                memory.save();
            }
            if (param.isDoPrint()) {
                System.out.println(memory);
            }
            memoryList.add(memory);
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

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return getDisableLSTM();
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.MLINDICATOR;
    }

    @Override
    public List<String> getConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASDELTAS);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATORINTERVALDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORFUTUREDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORTHRESHOLD);
        return list;
    }
    
    public List<String> getThreeConfList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORATR);
        list.add(ConfigConstants.AGGREGATORSINDICATORCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCHRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASATR);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCHRSI);
        return list;
    }
}

