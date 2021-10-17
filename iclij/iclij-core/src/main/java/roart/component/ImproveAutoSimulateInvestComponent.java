package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.ServiceConstants;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.SimConstants;
import roart.db.IclijDbDao;
import roart.iclij.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class ImproveAutoSimulateInvestComponent extends ComponentML {

    @Override
    public void enable(Map<String, Object> valueMap) {
    }

    @Override
    public void disable(Map<String, Object> valueMap) {
    }

    @Override
    public ComponentData handle(MarketAction action, Market market, ComponentData param, ProfitData profitdata,
            Memories positions, boolean evolve, Map<String, Object> aMap, String subcomponent, String mlmarket,
            Parameters parameters) {
        if (true) {
            SimulateInvestComponent component = new SimulateInvestComponent();
            component.setConfig(getConfig());
            ComponentData ret = component.handle(action, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
            Map<String, Double> scoreMap = ret.getScoreMap();
            List<Double> scores = new ArrayList<>();
            scores.add(scoreMap.get(SimConstants.SCORE));
            if (param.getInput().getConfig().getAutoSimulateInvestFutureCount() > 0) {
                SimulateInvestData newComponentData = new SimulateInvestData(param);
                newComponentData.setResultMaps(param.getResultMaps());
                newComponentData.setResultRebaseMaps(((SimulateInvestData)param).getResultRebaseMaps());
                IclijConfig config = newComponentData.getInput().getConfig();
                int count = config.getAutoSimulateInvestFutureCount();
                int time = config.getAutoSimulateInvestFutureTime();
                String startDateOrig = config.getAutoSimulateInvestStartdate();
                String endDateOrig = config.getAutoSimulateInvestEnddate();
                String startDate = TimeUtil.replace(endDateOrig);
                String endDate;
                for (int i = 0; i < count; i++) {
                    LocalDate start = null;
                    try {
                        start = TimeUtil.convertDate(startDate);
                    } catch (ParseException e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    LocalDate end = start.plusMonths(time);
                    endDate = TimeUtil.convertDate2(end);
                    config.getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, startDate);
                    config.getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, endDate);
                    ComponentData ret2 = component.handle(action, market, newComponentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
                    Map<String, Double> scoreMap2 = ret2.getScoreMap();
                    scores.add(scoreMap2.get(SimConstants.SCORE));
                    startDate = endDate;
                }
                scoreMap.put(SimConstants.SCORE, Collections.min(scores));
                config.getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, startDateOrig);
                config.getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, endDateOrig);
            }
            return ret;
        }
        ComponentData componentData = new ComponentData(param);

        componentData.setFuturedays(0);

        handle2(action, market, componentData, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters);
        return componentData;
    }

    @Override
    public ComponentData improve(MarketAction action, ComponentData componentparam, Market market, ProfitData profitdata,
            Memories positions, Boolean buy, String subcomponent, Parameters parameters, boolean wantThree,
            List<MLMetricsItem> mlTests) {
        SimulateInvestData param = new SimulateInvestData(componentparam);
        param.setAllIncDecs(getAllIncDecs(market, null, null));
        //param.setAllMemories(getAllMemories(market, null, null));
        param.setAllMetas(getAllMetas(componentparam));
        /*
        Integer adviser = (Integer) param.getInput().getConfig().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTADVISER);
        if (adviser != null) {
            SimulateInvestConfig config = new SimulateInvestConfig();
            config.setAdviser(adviser);
            param.setConfig(config);
        }
         */
        getResultMaps(param, market);
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        param.setStockDates(stockDates);
        List<String> confList = getConfList();

        int ga = param.getInput().getConfig().getEvolveGA();
        Evolve evolve = SimulateInvestEvolveFactory.factory(ga);
        String evolutionConfigString = param.getInput().getConfig().getImproveAutoSimulateInvestEvolutionConfig();
        EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);
        //evolutionConfig.setGenerations(3);
        //evolutionConfig.setSelect(6);

        Object filters = param.getConfigValueMap().remove(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
        filters = param.getInput().getValuemap().get(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
        //param.getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS, filters);
        Map<String, Object> confMap = new HashMap<>();
        // confmap
        ComponentData e = evolve.evolve(action, param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap , evolutionConfig, getPipeline(), this, confList);
        Map<String, Object> results = (Map<String, Object>) e.getResultMap();
        results.put(SimConstants.FILTER, filters);
        e.getService().send(ServiceConstants.SIMAUTO, results, param.getInput().getConfig());
        return e;
    }

    @Override
    public MLConfigs getOverrideMLConfig(ComponentData componentdata) {
        return null;
    }

    @Override
    public void calculateIncDec(ComponentData param, ProfitData profitdata, Memories positions, Boolean above,
            List<MLMetricsItem> mlTests, Parameters parameters) {
    }

    @Override
    public List<MemoryItem> calculateMemory(ComponentData param, Parameters parameters) throws Exception {
        return null;
    }

    @Override
    public String getPipeline() {
        return PipelineConstants.IMPROVEAUTOSIMULATEINVEST;
    }

    @Override
    protected List<String> getConfList() {
        List<String> confList = new ArrayList<>();
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTLASTCOUNT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTDELLIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTSCORELIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTAUTOSCORELIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISER);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISERLIMIT);
        return confList;
    }

    @Override
    public String getThreshold() {
        return null;
    }

    @Override
    public String getFuturedays() {
        return null;
    }

    @Override
    public Object[] calculateAccuracy(ComponentData componentparam) throws Exception {
        return new Object[] { componentparam.getScoreMap().get(SimConstants.SCORE) };
    }

    // duplicated
    private void getResultMaps(SimulateInvestData param, Market market) {
        //Map<String, List<Object>> objectMap = new HashMap<>();
        IclijConfig config = param.getInput().getConfig();

        Map<String, Object> aMap = new HashMap<>();
        // for improve evolver
        //List<MetaItem> metas = param.getService().getMetas();
        //MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        //ComponentData componentData = component.improve2(action, param, market, profitdata, null, buy, subcomponent, parameters, mlTests);
        // don't need these both here and in getevolveml?
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, true);
        aMap.put(ConfigConstants.INDICATORSMACD, true);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, true);
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCMERGECY, false);
        // different line
        param.getResultMap(null, aMap);
        Map<String, Map<String, Object>> mapsRebase = param.getResultMaps();
        param.setResultRebaseMaps(mapsRebase);

        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, false);
        // different line
        param.getResultMap(null, aMap);
        //Map<String, Map<String, Object>> maps = param.getResultMaps();
        //param.getAndSetWantedCategoryValueMap();
        /*
        for (Entry<String, Map<String, Object>> entry : maps.entrySet()) {
            String key = entry.getKey();
            System.out.println("key " + key);
            System.out.println("keys " + entry.getValue().keySet());
        }
         */
        //Integer cat = (Integer) maps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        //String catName = new MetaUtil().getCategory(meta, cat);
        //Map<String, Object> resultMaps = maps.get(catName);
        /*
        if (resultMaps != null) {
            Map<String, Object> macdMaps = (Map<String, Object>) resultMaps.get(PipelineConstants.INDICATORMACD);
            //System.out.println("macd"+ macdMaps.keySet());
            objectMap = (Map<String, List<Object>>) macdMaps.get(PipelineConstants.OBJECT);
        }
         */
        //return resultMaps;
    }

    private List<MetaItem> getAllMetas(ComponentData param) {
        return param.getService().getMetas();
    }

    private List<IncDecItem> getAllIncDecs(Market market, LocalDate investStart, LocalDate investEnd) {
        try {
            return IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }
    
    @Override
    protected void configSaves(ComponentData param, Map<String, Object> anUpdateMap, String subcomponent) {
    }
    
}
