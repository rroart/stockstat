package roart.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jenetics.AnyGene;
import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import roart.action.MarketAction;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.component.model.MLIndicatorData;
import roart.component.model.PredictorData;
import roart.constants.IclijConstants;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.ConfigMapChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.util.MLUtil;
import roart.iclij.util.MiscUtil;
import roart.result.model.ResultMeta;
import roart.iclij.service.ControlService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate;

public abstract class Component {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private ActionComponentConfig config;
    
    /*
    private Market market;

    private ComponentData param;
    
    private ProfitData profitdata;
    
    private List<Integer> positions;
    
    private Map<String, Object> valueMap;
    
    private boolean evolve;
  */  
    
    public ActionComponentConfig getConfig() {
        return config;
    }

    public void setConfig(ActionComponentConfig config) {
        this.config = config;
    }

    private Map<String, Object> evolveMap = new HashMap<>();
    
    public abstract void enable(Map<String, Object> valueMap);
    
    public abstract void disable(Map<String, Object> valueMap);
    
    public abstract ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters);
    
    public abstract ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests);

    protected abstract void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps);

    public void handle2(MarketAction action, Market market, ComponentData param, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters) {
        try {
            param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        } catch (IndexOutOfBoundsException e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        Map<String, Object> valueMap = new HashMap<>();
        new MLUtil().disabler(valueMap);
        List<Component> allComponents = action.getComponentFactory().getAllComponents();
        for (Component component : allComponents) {
            component.disable(valueMap);
        }
        this.subdisable(valueMap, subcomponent);
        this.enable(valueMap);
        this.subenable(valueMap, subcomponent);
        try {
            Map<String, Object> loadValues = mlLoads(param, null, market, null, subcomponent, mlmarket, action, parameters);
            valueMap.putAll(loadValues);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String pipeline = getPipeline();
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        Map<String, Object> scoreMap = new HashMap<>();
        if (evolve) {   
            long time0 = System.currentTimeMillis();
            evolveMap = handleEvolve(market, pipeline, evolve, param, subcomponent, scoreMap, null, parameters);
            if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) || !IclijConstants.IMPROVEFILTER.equals(param.getAction()) || !IclijConstants.IMPROVEABOVEBELOW.equals(param.getAction())) {
                Double score = null;
                String description = null;
                if (IclijConstants.EVOLVE.equals(param.getAction()) || IclijConstants.DATASET.equals(param.getAction())) {
                    score = scoreMap
                            .values()
                            .stream()
                            .mapToDouble(e -> (Double) e)
                            .max()
                            .orElse(-1);
                    if (scoreMap.size() > 1) {
                        description = scoreMap.values().stream().mapToDouble(e -> (Double) e).summaryStatistics().toString();
                    }
                }
                TimingItem timing = saveTiming(param, evolve, time0, score, null, subcomponent, mlmarket, description, parameters);
                param.getTimings().add(timing);
           }
        }
        valueMap.putAll(evolveMap);
        valueMap.putAll(aMap);
        long time0 = System.currentTimeMillis();
        if (!IclijConstants.EVOLVE.equals(param.getAction()) && !IclijConstants.DATASET.equals(param.getAction())) {
            Map<String, Object> resultMaps = param.getResultMap(pipeline, valueMap);
            param.setCategory(resultMaps);
            param.getAndSetCategoryValueMap();
            Map resultMaps2 = param.getResultMap();
            handleMLMeta(param, resultMaps2);
        }
        if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) && !IclijConstants.IMPROVEFILTER.equals(param.getAction()) && !IclijConstants.IMPROVEABOVEBELOW.equals(param.getAction()) && !IclijConstants.EVOLVE.equals(param.getAction())) {
            Double score = null;
            String description = null;
            if (IclijConstants.MACHINELEARNING.equals(param.getAction()) ) {
                try {
                    saveAccuracy(param);
                    Object[] result = calculateAccuracy(param);
                    score = (Double) result[0];
                    description = (String) result[1];
                    if (result[2] != null) {
                        description =  (String) result[2] + " " + description;
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            if (IclijConstants.CROSSTEST.equals(param.getAction()) ) {
                try {
                    List<MemoryItem> memories = null;
                    try {
                        memories = calculateMemory(param, parameters);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    if (memories != null && memories.size() > 1) {
                        DoubleSummaryStatistics summary = memories.stream().mapToDouble(MemoryItem::getConfidence).filter(Objects::nonNull).summaryStatistics();
                        score = summary.getAverage();
                        description = summary.toString();
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
            TimingItem timing = saveTiming(param, false, time0, score, null, subcomponent, mlmarket, description, parameters);
            param.getTimings().add(timing);
        }
    }

    protected void subenable(Map<String, Object> valueMap, String subcomponent) {
    }

    protected void subdisable(Map<String, Object> valueMap, String subcomponent) {        
    }

    public void enableDisable(ComponentData param, Memories positions, Map<String, Object> valueMap, Boolean above) {
        List<String>[] enableDisable = enableDisable(param, positions, above);
        List<String> enableML = enableDisable[0];
        List<String> disableML = enableDisable[1];
        enableDisable(valueMap, enableML, true);
        enableDisable(valueMap, disableML, false);
        log.info("Disable {}", disableML);
    }

    private TimingItem saveTiming(ComponentData param, boolean evolve, long time0, Double score, Boolean buy, String subcomponent, String mlmarket, String description, Parameters parameters) {
        TimingItem timing = new TimingItem();
        timing.setAction(param.getAction());
        timing.setBuy(buy);
        timing.setMarket(param.getInput().getMarket());
        timing.setMlmarket(mlmarket);
        timing.setEvolve(evolve);
        timing.setComponent(getPipeline());
        timing.setTime(time0);
        timing.setRecord(LocalDate.now());
        timing.setDate(param.getFutureDate());
        timing.setScore(score);
        timing.setSubcomponent(subcomponent);
        timing.setParameters(JsonUtil.convert(parameters));
        timing.setDescription(description);
        try {
            if (true || param.isDoSave()) {
                timing.save();
            }
            return timing;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters);

    public abstract EvolutionConfig getEvolutionConfig(ComponentData componentdata);
    
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

    public abstract void calculateIncDec(ComponentData param, ProfitData profitdata, Memories positions, Boolean above, List<MLMetricsItem> mlTests, Parameters parameters);

    public abstract List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception;

    public abstract String getPipeline();
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent, String mlmarket, MarketAction action, Parameters parameters) throws Exception;

    protected abstract EvolutionConfig getImproveEvolutionConfig(IclijConfig config);
    
    protected abstract List<String> getConfList();
    
    public abstract boolean wantEvolve(IclijConfig config);
    
    public abstract boolean wantImproveEvolve();
    
    // the current implementation
    
    public ComponentData improve(MarketAction action, ComponentData param, AbstractChromosome chromosome, String subcomponent, ChromosomeWinner winner, Boolean buy, Fitness fitness) {
        long time0 = System.currentTimeMillis();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
        evolution.setParallel(false);
        if (fitness != null) {
            evolution.fittest = fitness::fitness;
        }
        
        Map<String, String> retMap = new HashMap<>();
        try {
            List<String> individuals = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals);
            evolution.print(param.getMarket() + " " + subcomponent, fitness.titleText(), individuals);
            Map<String, Object> confMap = new HashMap<>();
            double score = winner.handleWinner(param, best, confMap);
            //confMap.put("score", "" + score);
            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return param;
    }

    private void configSaves(ComponentData param, Map<String, Object> anUpdateMap, String subcomponent) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object == null) {
                log.error("Config value null");
                continue;
            }
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(getPipeline());
            configItem.setDate(LocalDate.now());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setSubcomponent(subcomponent);
            String value = JsonUtil.convert(object);
            configItem.setValue(value);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }

    protected void loadme(ComponentData param, ConfigMapChromosome chromosome, Market market, List<String> confList, Boolean buy, String subcomponent, MarketAction action, Parameters parameters) {
        List<String> config = new ArrayList<>();
        
        Map<String, Object> map = null;
        try {
            map = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent, action.getActionData(), parameters);
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
    
    public abstract List<String>[] enableDisable(ComponentData param, Memories positions, Boolean above);

    public void enableDisable(Map<String, Object> map, List<String> list, boolean bool) {
        for (String key : list) {
            map.put(key, bool);
        }
    }
    
    /*
    // ?
    @Deprecated
    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        List<String> subComponents = getConfig().getSubComponents(market, param.getInput().getConfig(), null);
        for(String subComponent : subComponents) {
            ComponentData componentData = handle(action, market, param, profitdata, new ArrayList<>(), false, new HashMap<>(), subComponent, null, parameters);
            calculateMemory(componentData, parameters);
        }
        return null;
    }
    */
    
    public Object[] calculateAccuracy(ComponentData componentparam) throws Exception {
        Object[] result = new Object[3];
        ComponentMLData param = (ComponentMLData) componentparam;
        List<Double> testAccuracies = new ArrayList<>();
        if (param.getResultMeta() == null) {
            return result;
        }
        for (ResultMeta meta : param.getResultMeta()) {
            Double testaccuracy = meta.getTestAccuracy();
            if (testaccuracy != null) {
                testAccuracies.add(testaccuracy);
            }
        }
        double acc = testAccuracies
                .stream()
                .mapToDouble(e -> e)
                .max()
                .orElse(-1);
        if (acc < 0) {
            return result;
        } else {
            result[0] = acc;
            if (testAccuracies.size() > 1) {
                result[1] = testAccuracies.stream().mapToDouble(e -> e).summaryStatistics().toString();
            }
            result[2] = null;
            if (param.getResultMeta().size() > 1) {
                List<ResultMeta> metalist = param.getResultMeta()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getTestAccuracy() != null)
                .filter(e -> e.getTestAccuracy() == acc)
                .collect(Collectors.toList());
                result[2] = "";
                for(ResultMeta meta : metalist) {
                    result[2] = result[2] + meta.getThreshold().toString() + " " + meta.getSubType() + meta.getSubSubType() + " " + meta.getLearnMap();
                }
            }
            if (param.getResultMeta().size() == 1) {
                result[2] = param.getResultMeta().get(0).getThreshold().toString() + " " + param.getResultMeta().get(0).getLearnMap();
            }
            return result;
        }
    }

    public abstract String getThreshold();
    
    public abstract String getFuturedays();

    // the currently used implementation for marketfilter
    
    
    
    // the implementation for jenetics
    
    public ComponentData improveJ(MarketAction action, ComponentData param, Market market,
            ProfitData profitdata, Object object, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, EvolveJ evolveJ) {
        long time0 = System.currentTimeMillis();
        Map<String, Object> confMap = new HashMap<>();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        double score = evolveJ.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap,
                evolutionConfig, getPipeline());
        try {
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return param;
        
    }

    // a new implementation with better fitness outside of chromosome
    
    public void saveAccuracy(ComponentData componentparam) throws Exception {
        ComponentMLData param = (ComponentMLData) componentparam;
        if (param.getResultMeta() == null) {
            return;
        }
        for (ResultMeta meta : param.getResultMeta()) {
            if (meta.getMlName() == null) {
                continue;
            }
            MLMetricsItem item = new MLMetricsItem();
            item.setRecord(LocalDate.now());
            item.setDate(param.getFutureDate());
            item.setComponent(getPipeline());
            item.setMarket(param.getMarket());
            item.setSubcomponent(meta.getMlName() + " " + meta.getModelName());
            if (meta.getSubType() != null) {
                item.setLocalcomponent(meta.getSubType() + meta.getSubSubType());
            }
            item.setTestAccuracy(meta.getTestAccuracy());
            item.setLoss(meta.getLoss());
            item.setThreshold(meta.getThreshold());
            item.save();
        }
    }

    protected IncDecItem mapAdder(Map<String, IncDecItem> map, String key, Double add, Map<String, String> nameMap, LocalDate date, String market, String subcomponent, String localcomponent, String parameters) {
        IncDecItem val = map.get(key);
        if (val == null) {
            val = new IncDecItem();
            val.setRecord(LocalDate.now());
            val.setDate(date);
            val.setId(key);
            val.setComponent(getPipeline());
            val.setLocalcomponent("");
            val.setMarket(market);
            val.setDescription("");
            val.setName(nameMap.get(key));
            val.setParameters(parameters);
            val.setScore(0.0);
            val.setSubcomponent(subcomponent);
            map.put(key, val);
        }
        val.setScore(val.getScore() + add);
        String component = getPipeline();
        component = component != null ? component.substring(0, 3) : "";
        val.setDescription(val.getDescription() + component + " " + subcomponent + " " + localcomponent + ", ");
        if (val.getLocalcomponent() == null || localcomponent == null) {
            int jj = 0;
        }
        val.setLocalcomponent(val.getLocalcomponent().isEmpty() ? localcomponent : val.getLocalcomponent() + " " + localcomponent);
        return val;
    }

}

