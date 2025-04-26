package roart.iclij.evolution.fitness.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.action.MarketActionData;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.iclij.component.Component;
import roart.iclij.component.factory.ComponentFactory;
import roart.component.model.ComponentData;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.service.util.MarketUtil;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.component.util.IncDecUtil;

public class FitnessMarketFilter2 {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Object> map = new HashMap<>();

    private MarketActionData action;
    
    private Market market;
    
    private ComponentData param;
    
    private ProfitData profitdata;

    protected Boolean buy;

    protected String componentName;
    
    private String subcomponent;
    
    private Parameters parameters;

    private List<MLMetricsDTO> mlTests;

    private List<String> stockDates;

    private List<IncDecDTO> incdecs;
    
    public FitnessMarketFilter2(MarketActionData action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, List<String> stockDates, List<IncDecDTO> incdecs) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
        this.mlTests = mlTests;
        this.stockDates = stockDates;
        this.incdecs = incdecs;
    }

    public synchronized double fitness(MarketFilterChromosome chromosome) {
        market.setFilter(chromosome.getGene().getAllele());
        return new FitnessMarketFilterCommon().fitnessCommon(action, param, market, profitdata, buy, stockDates, incdecs, parameters, componentName, map);
    }

    public synchronized double fitness1(MarketFilterChromosome chromosome) {
        log.info("Fitness");
        List<MemoryDTO> memoryItems = null;
        WebData myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryDTOs(new ArrayList<>());
        myData.setUpdateMap2(new HashMap<>());
        //myData.profitData = new ProfitData();
        myData.setTimingMap(new HashMap<>());
        int b = param.getService().coremlconf.hashCode();
        boolean c = param.getService().coremlconf.wantIndicatorRecommender();
        Set<IncDecDTO> listInc = new HashSet<>(profitdata.getBuys().values());
        Set<IncDecDTO> listDec = new HashSet<>(profitdata.getSells().values());
        Set<IncDecDTO> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec);
        Trend incProp = null;
        // TODO getcontent
        incProp = extracted(chromosome, myData, listInc, listDec, mlTests);
        Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getService().getIclijConfig());
        PipelineData[] maps = param.getResultMaps();
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, true, null, inmemory);
        new IncDecUtil().filterIncDecs(param, market, profitdata, maps, false, null, inmemory);

        double memoryFitness = 0.0;
        double incdecFitness = 0.0;
        try {
            if (true) {
                int fitnesses = 0;
                double fitness = 0;
                long countDec = 0;
                long sizeDec = 0;
                if (buy == null || buy == false) {
                    List<Boolean> listDecBoolean = listDec.stream().map(IncDecDTO::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
                    countDec = listDecBoolean.stream().filter(i -> i).count();                            
                    sizeDec = listDecBoolean.size();
                }
                if (sizeDec != 0) {
                    fitness = ((double) countDec) / sizeDec;
                    fitnesses++;
                }
                long countInc = 0;                            
                long sizeInc = 0;
                if (buy == null || buy == true) {
                    List<Boolean> listIncBoolean = listInc.stream().map(IncDecDTO::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
                    countInc = listIncBoolean.stream().filter(i -> i).count();                            
                    sizeInc = listIncBoolean.size();
                }
                double fitness2 = 0;
                if (sizeInc != 0) {
                    fitness2 = ((double) countInc) / sizeInc;
                    fitnesses++;
                }
                double fitness3 = 0;
                if (fitnesses != 0) {
                    fitness3 = (fitness + fitness2) / fitnesses;
                }
                incdecFitness = fitness3;
                double fitness4 = 0;
                long size = sizeDec + sizeInc;
                if (size > 0) {
                    fitness4 = ((double)(countDec + countInc)) / size;
                }
                incdecFitness = fitness4;
                int minimum = param.getConfig().getImproveProfitFitnessMinimum();
                if (minimum > 0 && size < minimum) {
                    log.info("Fit sum too small {} < {}", size, minimum);
                    incdecFitness = 0;
                }
                log.info("Fit {} {} ( {} / {} ) {} ( {} / {} ) {} {} ( {} / {} )", incProp, fitness, countDec, sizeDec, fitness2, countInc, sizeInc, fitness3, fitness4, countDec + countInc, size);
                log.info("Fit #{} {}", this.hashCode(), chromosome.getGene().getAllele().toString());
            }
            //memoryItems = new MyFactory().myfactory(getConf(), PipelineConstants.MLMACD);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        double fitness = 0;
        memoryItems = myData.getMemoryDTOs();
        for (MemoryDTO memoryItem : memoryItems) {
            Double value = memoryItem.getConfidence();
            if (value == null) {
                int jj = 0;
                continue;
            }
            if (!value.isNaN()) {
                fitness += value;
            }
        }
        if (!memoryItems.isEmpty()) {
            fitness = fitness / memoryItems.size();
            memoryFitness = fitness;
        }
        // or rather verified incdec
        log.info("Fit {} {} {}", this.componentName, incdecFitness, memoryFitness);
        //configSaves(param, getMap());
        param.getUpdateMap().putAll(map);
        return incdecFitness;
    }

    public Trend extracted(MarketFilterChromosome chromosome, WebData myData, Collection<IncDecDTO> listInc, Collection<IncDecDTO> listDec, List<MLMetricsDTO> mlTests) {
        Trend incProp = null;
        try {
            int verificationdays = param.getConfig().verificationDays();
            Component component =  new ComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            //ProfitData profitdata = new ProfitData();
            myData.setProfitData(profitdata);
            boolean myevolve = component.wantImproveEvolve();
            if (!param.getService().coremlconf.wantIndicatorRecommender()) {
                int jj = 0;
            }
            map.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            map.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            map.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            map.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);
            map.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            map.put(ConfigConstants.MISCMYDAYS, 0);

            String key = component.getThreshold();
            map.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            map.put(key2, parameters.getFuturedays());

            map.put(ConfigConstants.MISCTHRESHOLD, null);

            /*
            FindProfitAction myaction = new FindProfitAction();
            //marketTime.;
            MarketComponentTime marketTime = myaction.getMCT(componentName, component, subcomponent, market, 0, false, buy, parameters);
            //marketTime.component = component;
            myaction.getPicksFiltered(myData, param, param.getInput().getConfig(),  marketTime, evolve);                
        
            // plus borrow from verifyprofit
            //myaction.filterIncDecs(param, market, profitdata, maps, true);
            */
            ComponentData componentData = component.handle(action, market, param, profitdata, new Memories(market), myevolve /*evolve && evolvefirst*/, map, subcomponent, null, parameters, false);
            //componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryDTO> memories;
            try {
                memories = component.calculateMemory(action, componentData, parameters);
                if (memories == null || memories.isEmpty()) {
                    int jj = 0;
                }
                myData.getMemoryDTOs().addAll(memories);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            Memories listMap =  new Memories(market);
            ProfitInputData inputdata = new ProfitInputData();
            listMap.method(myData.getMemoryDTOs(), param.getConfig());        
            //ProfitData profitdata = new ProfitData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());
            Memories positions = listMap;

            //component.enableDisable(componentData, positions, param.getConfigValueMap(), buy);

            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, map, subcomponent, null, parameters, false);
            component.calculateIncDec(componentData2, profitdata, positions, buy, mlTests, parameters);

            short startoffset = new MarketUtil().getStartoffset(market);
            //action.setValMap(param);
            // todo two getcontent
            param.getAndSetWantedCategoryValueMap(false);
            // todo clean
            VerifyProfit verify = new VerifyProfit();
            incProp = verify.getTrend(verificationdays, param.getCategoryValueMap(), startoffset, null);
            //Trend incProp = new FindProfitAction().getTrend(verificationdays, param.getFutureDate(), param.getService());
            //log.info("trendcomp {} {}", trend, incProp);
            if (verificationdays > 0) {
                try {
                    //param.setFuturedays(verificationdays);
                    param.setFuturedays(0);
                    param.setOffset(0);
                    param.setDates(null, null, action, market);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }            
                // TODO fixed getcontent number two
                new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold(), param, null, market);
            }
            Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getConfig().getInmemoryServer(), param.getConfig().getInmemoryHazelcast(), param.getConfig().getInmemoryRedis());
            new PipelineThreadUtils(param.getConfig(), inmemory, param.getService().getIo().getCuratorClient()).cleanPipeline(param.getService().id, param.getId());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incProp;
    }

}
