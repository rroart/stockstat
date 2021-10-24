package roart.iclij.component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentMLData;
import roart.constants.IclijConstants;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.winner.ChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.Fitness;
import roart.evolution.species.Individual;
import roart.iclij.component.factory.ComponentFactory;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.util.MLUtil;
import roart.iclij.util.MiscUtil;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;

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
    
    public abstract ComponentData handle(MarketActionData action, Market market, ComponentData param, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters, boolean hasParent);
    
    public abstract ComponentData improve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests, Fitness fitness, boolean save);

    public abstract void handleMLMeta(ComponentData param, Map<String, List<Object>> mlMaps);

    /*
     * We need to handle 11 actions
     * 
     * Ordinary:
     * CrossTest
     * FindProfit
     * MachineLearning
     * 
     * Evolving:
     * Evolve: Uses ML in core
     * Dataset (goes external)
     * ImproveAboveBelow (saves in improve)
     * ImproveAutoSimulateInvest (manual)
     * ImproveFilter (saves in improve)
     * ImproveProfit (saves in improve)
     * ImproveSimulateInvest (manual)
     * SimulateInvest (manual)
     */
    
    public void handle2(MarketActionData action, Market market, ComponentData param, ProfitData profitdata, Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket, Parameters parameters, boolean hasParent) {
        /*
        try {
            param.setDates(null, null, action, market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        } catch (IndexOutOfBoundsException e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
        */
        Map<String, Object> valueMap = new HashMap<>();
        new MLUtil().disabler(valueMap);
        List<Component> allComponents = new ComponentFactory().getAllComponents();
        for (Component component : allComponents) {
            component.disable(valueMap);
        }
        if (subcomponent != null) {
            this.subdisable(valueMap, subcomponent);
        }
        this.enable(valueMap);
        if (subcomponent != null) {
            this.subenable(valueMap, subcomponent);
        }
        if (action.wantsUpdate(param.getInput().getConfig())) {
        	if (IclijConstants.FINDPROFIT.equals(action.getName())) {
            	valueMap.putAll(getValueMap(action, IclijConstants.MACHINELEARNING, market, param, subcomponent, mlmarket, parameters));        		
        	}
        	valueMap.putAll(getValueMap(action, param.getAction(), market, param, subcomponent, mlmarket, parameters));
        }
        String pipeline = getPipeline();
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        Map<String, Object> scoreMap = new HashMap<>();
        Object[] scoreDescription = new Object[] { null, null };
        long time0 = System.currentTimeMillis();
        if (evolve) {   
            EvolutionConfig actionEvolveConfig = JsonUtil.convert(action.getEvolutionConfig(param.getInput().getConfig()), EvolutionConfig.class);
            evolveMap = handleEvolve(market, pipeline, evolve, param, subcomponent, scoreMap, null, parameters, actionEvolveConfig, action.getMLConfig(param.getInput().getConfig()));
            //if (IclijConstants.EVOLVE.equals(param.getAction())) {
            //    action.saveTimingCommon(this, param, subcomponent, mlmarket, parameters, scoreMap, time0, evolve);
           //}
        }
        valueMap.putAll(evolveMap);
        valueMap.putAll(aMap);
        if (action.doHandleMLMeta()) {
        handleMLMetaCommon(param, valueMap);
        }
        try {
        if (IclijConstants.MACHINELEARNING.equals(action.getName())) {
        	saveAccuracy(param);
        }
         saveTiming(this, param, subcomponent, mlmarket, parameters, scoreMap, time0, false, hasParent, action);
        } catch (Exception e) {
        	log.error(Constants.EXCEPTION, e);
        }
    }

	private Map<String, Object> getValueMap(MarketActionData action, String actionName, Market market, ComponentData param,
			String subcomponent, String mlmarket, Parameters parameters) {
        Map<String, Object> valueMap = new HashMap<>();
		try {
			Map<String, Object> loadValues = mlLoads(param, null, market, actionName, null, subcomponent, mlmarket, action, parameters);
			if (!loadValues.keySet().contains(IclijConstants.ALL)) {
				valueMap.putAll(loadValues);
			} else {
				String val = (String) loadValues.get(IclijConstants.ALL);
				Map<String, Object> map = JsonUtil.convert(val, Map.class);
				valueMap.putAll(map);
			}
			log.info("Loaded values {}", loadValues);
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		}
		return valueMap;
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

    public TimingItem saveTiming(ComponentData param, boolean evolve, long time0, Double score, Boolean buy, String subcomponent, String mlmarket, String description, Parameters parameters, boolean save) {
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
            if (param.isDoSave() || save) {
                timing.save();
            }
            return timing;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param, String subcomponent, Map<String, Object> scoreMap, String mlmarket, Parameters parameters, EvolutionConfig actionEvolveConfig, String actionML);

    public abstract EvolutionConfig getEvolutionConfig(ComponentData componentdata, EvolutionConfig actionEvolutionConfig);
    
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
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market, String action, Boolean buy, String subcomponent, String mlmarket, MarketActionData actionData, Parameters parameters) throws Exception;

    protected abstract List<String> getConfList();
    
    public List<String> getConflist() {
        return getConfList();
    }
    
    public abstract boolean wantEvolve(IclijConfig config);
    
    public abstract boolean wantImproveEvolve();
    
    // the current implementation
    
    public ComponentData improve(MarketActionData action, ComponentData param, AbstractChromosome chromosome, String subcomponent, ChromosomeWinner winner, Boolean buy, Fitness fitness, boolean save) {
        long time0 = System.currentTimeMillis();
        EvolutionConfig evolutionConfig = JsonUtil.convert(action.getEvolutionConfig(param.getInput().getConfig()), EvolutionConfig.class);
        EvolutionConfig evolveConfig = getEvolutionConfig(param, evolutionConfig);
        evolutionConfig = evolveConfig;
        OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
        evolution.setParallel(false);
        if (fitness != null) {
            evolution.fittest = fitness::fitness;
        }
        
        Map<String, String> retMap = new HashMap<>();
        try {
            List<String> individuals = new ArrayList<>();
            List<Pair<Double, AbstractChromosome>> results = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals, results);
            String mysubcomponent = nullString(subcomponent);
            String title = action.getName() + " " + param.getMarket() + " " + getPipeline() + mysubcomponent;
            String subtitle = fitness.subTitleText();
            String filename = evolution.print(title + nullString(fitness.titleText()), fitness.subTitleText(), individuals);
            Map<String, Object> confMap = new HashMap<>();
            double score = winner.handleWinner(param, best, confMap);
            //confMap.put("score", "" + score);
            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put("" + score, score);
            param.setScoreMap(scoreMap);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(filename, results);
            resultMap.put(EvolveConstants.TITLETEXT, title + nullString(fitness.titleText()));
            resultMap.put(EvolveConstants.SUBTITLETEXT, subtitle);
            resultMap.put(EvolveConstants.ID, filename);
            //resultMap.put("id", filename);
            param.setResultMap(resultMap);
            //param.setFutureDate(Lo1610910447946.txtcalDate.now());
            // fix mlmarket;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null, save);
            param.getTimings().add(timing);
            //if (!(this instanceof ImproveSimulateInvestComponent) && !(this instanceof ImproveAutoSimulateInvestComponent)) {
            configSaves(param, confMap, subcomponent);
            //}
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return param;
    }

    private String nullString(String string) {
        if (string == null) {
            return "";
        } else {
            return " " + string;
        }
    }
    
    protected void configSaves(ComponentData param, Map<String, Object> anUpdateMap, String subcomponent) {
        // TODO save whole?
        //for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = IclijConstants.ALL;
            Object object = JsonUtil.convert(anUpdateMap);
            if (object == null) {
                log.error("Config value null");
                return;
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
        //}
    }

    protected void loadme(ComponentData param, ConfigMapChromosome2 chromosome, Market market, List<String> confList, Boolean buy, String subcomponent, MarketActionData action, Parameters parameters) {
        List<String> config = new ArrayList<>();
        
        Map<String, Object> map = null;
        try {
            map = new MiscUtil().loadConfig(param.getService(), param.getInput(), market, market.getConfig().getMarket(), param.getAction(), getPipeline(), false, buy, subcomponent, action, parameters);
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
    /*
    public ComponentData improveJ(MarketActionData action, ComponentData param, Market market,
            ProfitData profitdata, Object object, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, EvolveJ evolveJ) {
        long time0 = System.currentTimeMillis();
        Map<String, Object> confMap = new HashMap<>();
        EvolutionConfig evolutionConfig = JsonUtil.convert(action.getEvolutionConfig(param.getInput().getConfig()), EvolutionConfig.class);
        EvolutionConfig evolveConfig = getEvolutionConfig(param, evolutionConfig);
        param = evolveJ.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap,
                evolveConfig, getPipeline(), null, null);
        try {
            // fix mlmarket;
            double score = 0;
            TimingItem timing = saveTiming(param, true, time0, score, buy, subcomponent, null, null, null, action.getParent() != null);
            param.getTimings().add(timing);
            configSaves(param, confMap, subcomponent);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return param;
        
    }
*/
    
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
            item.setTrainAccuracy(meta.getTrainAccuracy());
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

    protected IncDecItem mapAdder2(Map<String, IncDecItem> map, String key, Double add, Map<String, String> nameMap, LocalDate date, String market, String subcomponent, String localcomponent, String parameters) {
        String newkey = key + date;
        IncDecItem val = map.get(newkey);
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
            map.put(newkey, val);
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

    protected void handleMLMetaCommon(ComponentData param, Map<String, Object> valueMap) {
        Map<String, Object> resultMaps = param.getResultMap(getPipeline(), valueMap);
        param.setCategory(resultMaps);
        param.getAndSetCategoryValueMap();
        Map resultMaps2 = param.getResultMap();
        handleMLMeta(param, resultMaps2);        
    }
    
    public void saveTiming(Component component, ComponentData param, String subcomponent, String mlmarket,
            Parameters parameters, Map<String, Object> scoreMap, long time0, boolean evolve, boolean hasParent, MarketActionData action) throws Exception {
        saveTimingCommon(component, param, subcomponent, mlmarket, parameters, scoreMap, time0, evolve, hasParent, action);
    }
    
    public void saveTimingCommon(Component component, ComponentData param, String subcomponent, String mlmarket,
            Parameters parameters, Map<String, Object> scoreMap, long time0, boolean evolve, boolean hasParent, MarketActionData action) throws Exception {
        Object[] scoreDescription;
        scoreDescription = action.getScoreDescription(calculateAccuracy(param), scoreMap);
        Double score = (Double) scoreDescription[0];
        String description = (String) scoreDescription[1];
        TimingItem timing = component.saveTiming(param, evolve, time0, score, null, subcomponent, mlmarket, description, parameters, hasParent);
        param.getTimings().add(timing);
    }
}

