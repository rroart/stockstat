package roart.component;

import java.io.IOException;
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
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
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
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate;

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
        valueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, Boolean.FALSE);
    }
    
    public abstract ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters);
    
    public abstract ComponentData improve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, List<Integer> positions, Boolean buy, String subcomponent, Parameters parameters);

    protected abstract void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps);

    public void handle2(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters) {
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
            if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) || !IclijConstants.IMPROVEFILTER.equals(param.getAction())) {
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
        if (!IclijConstants.IMPROVEPROFIT.equals(param.getAction()) && !IclijConstants.IMPROVEFILTER.equals(param.getAction()) && !IclijConstants.EVOLVE.equals(param.getAction())) {
            Double score = null;
            String description = null;
            if (IclijConstants.MACHINELEARNING.equals(param.getAction()) ) {
                try {
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

    public void enableDisable(ComponentData param, List<Integer> positions, Map<String, Object> valueMap) {
        List<String>[] enableDisable = enableDisable(param, positions);
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
            timing.save();
            return timing;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters);

    public abstract EvolutionConfig getEvolutionConfig(ComponentData componentdata);
    
    public abstract EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata);
    
    public abstract Map<String, EvolveMLConfig> getMLConfig(Market market, ComponentData componentdata, String mlmarket);

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

    public abstract void calculateIncDec(ComponentData param, ProfitData profitdata, List<Integer> positions, Boolean above);

    public abstract List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception;

    public abstract String getPipeline();
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, Boolean buy, String subcomponent, String mlmarket, MarketAction action, Parameters parameters) throws Exception;

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
            List<String> individuals = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals);
            evolution.print(param.getMarket() + " " + subcomponent, individuals);
            ConfigMapChromosome bestChromosome = (ConfigMapChromosome) best.getEvaluation();
            Map<String, Object> confMap = bestChromosome.getMap();
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            double score = best.getFitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, chromosome.getBuy(), subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
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
            map = ServiceUtil.loadConfig(param, market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent, action, parameters);
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
    
    public abstract List<String> getSubComponents(Market market, ComponentData componentData, String mlmarket);

    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve, Map<String, Object> aMap) throws Exception {
        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        List<String> subComponents = getSubComponents(market, param, null);
        for(String subComponent : subComponents) {
            ComponentData componentData = handle(action, market, param, profitdata, new ArrayList<>(), false, new HashMap<>(), subComponent, null, parameters);
            calculateMemory(componentData, parameters);
        }
        return null;
    }
    
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

    public abstract int getPriority(IclijConfig conf);

    public int getPriority(IclijConfig conf, String key) {
        Integer value = (Integer) conf.getConfigValueMap().get(key + "[@priority]");
        return value != null ? value : 0;
    }
    
    public abstract String getThreshold();
    
    public abstract String getFuturedays();

    public ComponentData improve2(MarketAction action, ComponentData param, Market market,
            ProfitData profitdata, Object object, Boolean buy, String subcomponent, Parameters parameters) {
        long time0 = System.currentTimeMillis();
        //Main main = new Main();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
        evolution.setParallel(false);
        
        Map<String, String> retMap = new HashMap<>();
        try {
            MarketFilterGene gene = new MarketFilterGene(market.getFilter());
            MarketFilterChromosome chromosome = new MarketFilterChromosome(action, new ArrayList<>(), param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene);
            List<String> individuals = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals );
            MarketFilterChromosome bestChromosome = (MarketFilterChromosome) best.getEvaluation();
            evolution.print(param.getMarket() + " " + subcomponent, individuals);
            MarketFilter aConf = bestChromosome.getGene().getMarketfilter();
            Map<String, Object> confMap = new HashMap<>();
            confMap.put("some", JsonUtil.convert(aConf));
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            double score = best.getFitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
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
    
    public ComponentData improve3(MarketAction action, ComponentData param, Market market,
            ProfitData profitdata, Object object, Boolean buy, String subcomponent, Parameters parameters) {
        long time0 = System.currentTimeMillis();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        FitnessMarketFilter2 fit = new FitnessMarketFilter2(action, new ArrayList<>(), param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters);
        final Codec<roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome, roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene> codec = Codec.of(Genotype.of(new roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome(new MarketFilter())),gt -> (roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome) gt.getChromosome());
        final Engine<AnyGene<roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene>, Double> engine = Engine
                .builder(fit::fitness, codec)
                .populationSize(evolutionConfig.getSelect())
                .alterers(
                        //new MeanAlterer<>(0.175),
                        new roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate(),
                        new roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover<>(0.5)
                        )
                .build();
        
        Map<String, String> retMap = new HashMap<>();
        try {
            MarketFilterGene gene = new MarketFilterGene(market.getFilter());
            MarketFilterChromosome chromosome = new MarketFilterChromosome(action, new ArrayList<>(), param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene);
            List<String> individuals = new ArrayList<>();
            final Phenotype<AnyGene<roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene>, Double> pt = engine.stream()
                    .limit(evolutionConfig.getGenerations())
                    .collect(EvolutionResult.toBestPhenotype());

            System.out.println(pt);
            MarketFilter aConf = pt.genotype().chromosome().gene().allele().getAllele();
            Map<String, Object> confMap = new HashMap<>();
            confMap.put("some", JsonUtil.convert(aConf));
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            double score = pt.fitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
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
    
    public ComponentData improve4(MarketAction action, ComponentData param, Market market,
            ProfitData profitdata, Object object, Boolean buy, String subcomponent, Parameters parameters) {
        long time0 = System.currentTimeMillis();
        //Main main = new Main();
        EvolutionConfig evolutionConfig = getImproveEvolutionConfig(param.getInput().getConfig());
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
        evolution.setParallel(false);
        
        Map<String, String> retMap = new HashMap<>();
        try {
            MarketFilterGene gene = new MarketFilterGene(market.getFilter());
            MarketFilterChromosome2 chromosome = new MarketFilterChromosome2(new ArrayList<>(), gene);
            List<String> individuals = new ArrayList<>();
            FitnessMarketFilter fit = new FitnessMarketFilter(action, new ArrayList<>(), param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters);
            evolution.fittest = fit::fitness;
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals );
            MarketFilterChromosome2 bestChromosome = (MarketFilterChromosome2) best.getEvaluation();
            evolution.print(param.getMarket() + " " + subcomponent, individuals);
            MarketFilter aConf = bestChromosome.getGene().getMarketfilter();
            Map<String, Object> confMap = new HashMap<>();
            confMap.put("some", JsonUtil.convert(aConf));
            param.setUpdateMap(confMap);
            Map<String, Double> scoreMap = new HashMap<>();
            double score = best.getFitness();
            //confMap.put("score", "" + score);
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            param.setFutureDate(LocalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
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

