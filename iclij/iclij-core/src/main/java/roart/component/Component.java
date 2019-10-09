package roart.component;

import java.text.ParseException;
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

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLIndicatorData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public abstract class Component {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /*
    private Market market;

    private ComponentData param;
    
    private ProfitData profitdata;
    
    private List<Integer> positions;
    
    private Map<String, Object> valueMap;
    
    private boolean evolve;
  */  
    
    private Map<String, Object> evolveMap = new HashMap<>();
    
    public abstract void enable(Map<String, Object> valueMap);
    
    public abstract void disable(Map<String, Object> valueMap);
    
    public static void disabler(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        valueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, Boolean.FALSE);
    }
    
    public abstract ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent);
    
    public abstract ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy, String subcomponent);

    protected abstract void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps);

    public void handle2(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent) {
        try {
            param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        } catch (IndexOutOfBoundsException e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        Map<String, Object> valueMap = new HashMap<>();
        Component.disabler(valueMap);
        List<Component> allComponents = action.getComponentFactory().getAllComponents();
        for (Component component : allComponents) {
            component.disable(valueMap);
        }
        this.subdisable(valueMap, subcomponent);
        this.enable(valueMap);
        this.subenable(valueMap, subcomponent);
        try {
            Map<String, Object> loadValues = mlLoads(param, null, market, null, subcomponent);
            valueMap.putAll(loadValues);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String pipeline = getPipeline();
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        if (evolve) {   
            long time0 = System.currentTimeMillis();
            evolveMap = handleEvolve(market, pipeline, evolve, param, subcomponent);
            if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) ) {
                TimingItem timing = saveTiming(param, evolve, time0, null, null, subcomponent);
                param.getTimings().add(timing);
           }
        }
        valueMap.putAll(evolveMap);
        valueMap.putAll(aMap);
        long time0 = System.currentTimeMillis();
        Map<String, Object> resultMaps = param.getResultMap(pipeline, valueMap);
        param.setCategory(resultMaps);
        param.getAndSetCategoryValueMap();
        Map resultMaps2 = param.getResultMap();
        handleMLMeta(param, resultMaps2);
        if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) ) {
            Double score = null;
            if (!IclijConstants.FINDPROFIT.equals(param.getAction()) ) {
                try {
                    score = calculateAccuracy(param);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            TimingItem timing = saveTiming(param, false, time0, score, null, subcomponent);
            param.getTimings().add(timing);
        }
    }

    protected void subenable(Map<String, Object> valueMap, String subcomponent) {
    }

    protected void subdisable(Map<String, Object> valueMap, String subcomponent) {        
    }

    public void enableDisable(ComponentData param, List<Integer> positions, Map<String, Object> valueMap) {
        List<String>[] enableDisable = enableDisable(param, positions);
        List<String> enableML = enableDisable[0];
        List<String> disableML = enableDisable[1];
        enableDisable(valueMap, enableML, true);
        enableDisable(valueMap, disableML, false);
        log.info("Disable {}", disableML);
    }

    private TimingItem saveTiming(ComponentData param, boolean evolve, long time0, Double score, Boolean buy, String subcomponent) {
        TimingItem timing = new TimingItem();
        timing.setAction(param.getAction());
        timing.setBuy(buy);
        timing.setMarket(param.getInput().getMarket());
        timing.setEvolve(evolve);
        timing.setComponent(getPipeline());
        timing.setTime(time0);
        timing.setRecord(LocalDate.now());
        timing.setDate(param.getFutureDate());
        timing.setScore(score);
        timing.setSubcomponent(subcomponent);
        try {
            timing.save();
            return timing;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent);

    public abstract EvolutionConfig getEvolutionConfig(ComponentData componentdata);
    
    public abstract EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata);
    
    public abstract Map<String, EvolveMLConfig> getMLConfig(Market market, ComponentData componentdata);

    public abstract String getLocalMLConfig(ComponentData componentdata);

    public abstract MLConfigs getOverrideMLConfig(ComponentData componentdata);
    
    public void set(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions,
            boolean evolve) {
        /*
        this.market = market;
        this.param = param;
        this.profitdata = profitdata;
        this.positions = positions;
        this.valueMap = valueMap;
        this.evolve = evolve;
        */
    }

    public abstract void calculateIncDec(ComponentData param, ProfitData profitdata, List<Integer> positions);

    public abstract List<MemoryItem> calculateMemory(ComponentData param) throws Exception;

    public abstract String getPipeline();
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent) throws Exception;

    protected abstract EvolutionConfig getImproveEvolutionConfig(IclijConfig config);
    
    protected abstract List<String> getConfList();
    
    public abstract boolean wantEvolve(IclijConfig config);
    
    public abstract boolean wantImproveEvolve();
    
    public ComponentData improve(MarketAction action, ComponentData param, ConfigMapChromosome chromosome, String subcomponent) {
        long time0 = System.currentTimeMillis();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
        evolution.setParallel(false);
        
        Map<String, String> retMap = new HashMap<>();
        try {
            Individual best = evolution.getFittest(evolutionConfig, chromosome);
            ConfigMapChromosome bestChromosome = (ConfigMapChromosome) best.getEvaluation();
            Map<String, Object> confMap = bestChromosome.getMap();
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            double score = best.getFitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            TimingItem timing = saveTiming(param, true, time0, score, chromosome.getBuy(), subcomponent);
            param.getTimings().add(timing);
            if (false) {
                ConfigItem configItem = new ConfigItem();
                configItem.setAction(param.getAction());
                configItem.setComponent(getPipeline());
                configItem.setDate(param.getBaseDate());
                configItem.setId("score " + confMap.keySet());
                configItem.setMarket(param.getMarket());
                configItem.setRecord(LocalDate.now());
                configItem.setValue("" + score);
                try {
                    configItem.save();
                } catch (Exception e) {
                    log.info(Constants.EXCEPTION, e);
                }                
            }
            //bestChromosome.get
            //Map<String, Object> confMap = null;
            //String marketName = profitdata.getInputdata().getListMap().values().iterator().next().get(0).getMarket();
            //ControlService srv = new ControlService();
            //srv.getConfig();    
            /*
            List<Double> newConfidenceList = new ArrayList<>();
            //srv.conf.getConfigValueMap().putAll(confMap);
            List<MemoryItem> memories = new MLService().doMLMACD(new ComponentInput(marketName, null, null, false, false), confMap);
            for(MemoryItem memory : memories) {
                newConfidenceList.add(memory.getConfidence());
            }
            log.info("New confidences {}", newConfidenceList);
            retMap.put("key", newConfidenceList.toString());
            */
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return param;
    }

    protected void loadme(ComponentData param, ConfigMapChromosome chromosome, Market market, List<String> confList, Boolean buy, String subcomponent) {
        List<String> config = new ArrayList<>();
        
        Map<String, Object> map = null;
        try {
            map = ServiceUtil.loadConfig(param, market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (map != null && !map.isEmpty()) {
            Set<String> keys = map.keySet();
            for (String key : confList) {
                if (!keys.contains(key)) {
                    map.remove(key);
                }
            }
            /*
            String configStr = (String) map.get(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER);
            if (configStr != null) {
                config = JsonUtil.convert(configStr, new TypeReference<List<String>>() { });
            }
            */
        }

        chromosome.setMap(map);

    }
    
    public abstract List<String>[] enableDisable(ComponentData param, List<Integer> positions);

    public void enableDisable(Map<String, Object> map, List<String> list, boolean bool) {
        for (String key : list) {
            map.put(key, bool);
        }
    }
    
    public abstract List<String> getSubComponents(Market market, ComponentData componentData);

    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) throws Exception {
        List<String> subComponents = getSubComponents(market, param);
        for(String subComponent : subComponents) {
            ComponentData componentData = handle(action, market, param, profitdata, new ArrayList<>(), false, new HashMap<>(), subComponent);
            calculateMemory(componentData);
        }
        return null;
    }
    
    public Double calculateAccuracy(ComponentData componentparam) throws Exception {
        ComponentMLData param = (ComponentMLData) componentparam;
        List<Double> testAccuracies = new ArrayList<>();
        if (param.getResultMeta() == null) {
            return null;
        }
        for (ResultMeta meta : param.getResultMeta()) {
            Double testaccuracy = meta.getTestAccuracy();
            if (testaccuracy != null) {
                testAccuracies.add(testaccuracy);
            }
        }
        return testAccuracies
                .stream()
                .mapToDouble(e -> e)
                .average()
                .orElse(0);
    }

}

