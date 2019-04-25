package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.MLIndicatorData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
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
    }
    
    public abstract ComponentData handle(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap);
    
    public abstract ComponentData improve(ComponentData param, Market market, ProfitData profitdata, List<Integer> positions);

    public void handle2(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) {
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
        this.enable(valueMap);
        try {
            Map<String, Object> loadValues = mlLoads(param, null, market);
            valueMap.putAll(loadValues);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String pipeline = getPipeline();
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        if (evolve) {   
            long time0 = System.currentTimeMillis();
            evolveMap = handleEvolve(market, pipeline, evolve, param);
            if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) ) {
                saveTiming(param, evolve, time0, null);
            }
        }
        valueMap.putAll(evolveMap);
        valueMap.putAll(aMap);
        long time0 = System.currentTimeMillis();
        Map<String, Object> resultMaps = param.getResultMap(pipeline, valueMap);
        param.setCategory(resultMaps);
        param.getAndSetCategoryValueMap();
        if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) ) {
            saveTiming(param, false, time0, null);
        }
    }

    private void saveTiming(ComponentData param, boolean evolve, long time0, Double score) {
        TimingItem timing = new TimingItem();
        timing.setAction(param.getAction());
        timing.setMarket(param.getInput().getMarket());
        timing.setEvolve(evolve);
        timing.setComponent(getPipeline());
        timing.setTime(time0);
        timing.setRecord(LocalDate.now());
        timing.setDate(param.getFutureDate());
        timing.setScore(score);
        try {
            timing.save();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param);

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
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market) throws Exception;

    protected abstract EvolutionConfig getImproveEvolutionConfig(IclijConfig config);
    
    protected abstract List<String> getConfList();
    
    public abstract boolean wantEvolve(IclijConfig config);
    
    public abstract boolean wantImproveEvolve();
    
    public ComponentData improve(ComponentData param, ConfigMapChromosome chromosome) {
        long time0 = System.currentTimeMillis();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);

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
            saveTiming(param, true, time0, score);
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


}

