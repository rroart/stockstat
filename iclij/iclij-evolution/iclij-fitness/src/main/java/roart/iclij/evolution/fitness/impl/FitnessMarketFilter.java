package roart.iclij.evolution.fitness.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.iclij.component.Component;
import roart.iclij.component.factory.ComponentFactory;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.Fitness;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FitnessMarketFilter extends Fitness {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Object> map = new HashMap<>();

    private MarketActionData action;
    
    private Market market;
    
    private ComponentData param;
    
    private ProfitData profitdata = new ProfitData();

    protected Boolean buy;

    protected String componentName;
    
    private String subcomponent;
    
    private Parameters parameters;
    
    private List<MLMetricsItem> mlTests;

    private List<String> stockDates;

    private List<IncDecItem> incdecs;
    
    public FitnessMarketFilter(MarketActionData action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, List<String> stockDates, List<IncDecItem> incdecs) {
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

    public double fitness1(AbstractChromosome chromosome) {
        List<MemoryItem> memoryItems = null;
        WebData myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryItems(new ArrayList<>());
        myData.setUpdateMap2(new HashMap<>());
        //myData.profitData = new ProfitData();
        myData.setTimingMap(new HashMap<>());
        int b = param.getService().conf.hashCode();
        boolean c = param.getService().conf.wantIndicatorRecommender();
        Set<IncDecItem> listInc = new HashSet<>(profitdata.getBuys().values());
        Set<IncDecItem> listDec = new HashSet<>(profitdata.getSells().values());
        Set<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec);
        Trend incProp = null;
        Trend incProp1 = null;
        try {
            int verificationdays = param.getConfig().verificationDays();
            Component component =  new ComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            //ProfitData profitdata = new ProfitData();
            myData.setProfitData(profitdata);
            boolean myevolve = component.wantImproveEvolve();
            if (!param.getService().conf.wantIndicatorRecommender()) {
                int jj2 = 0;
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
        
            market.setFilter(((MarketFilterChromosome2)chromosome).getGene().getMarketfilter());
            ComponentData componentData = component.handle(action, market, param, profitdata, new Memories(market), myevolve /*evolve && evolvefirst*/, map, subcomponent, null, parameters, false);
            //componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
        
            Memories listMap = new Memories(market);
            listMap.method(myData.getMemoryItems(), param.getConfig());        
            //ProfitData profitdata = new ProfitData();
            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());
            Memories positions = listMap;
        
            //component.enableDisable(componentData, positions, param.getConfigValueMap(), buy);
        
            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, map, subcomponent, null, parameters, false);
            component.calculateIncDec(componentData2, profitdata, positions, buy, mlTests, parameters);
        
            short startoffset = new MarketUtil().getStartoffset(market);
            //action.setValMap(param);
            param.getAndSetWantedCategoryValueMap();
            VerifyProfit verify = new VerifyProfit();
            incProp1 = verify.getTrend(verificationdays, param.getCategoryValueMap(), startoffset, null);
            //Trend incProp = new FindProfitAction().getTrend(verificationdays, param.getFutureDate(), param.getService());
            //log.info("trendcomp {} {}", trend, incProp);
            if (verificationdays > 0) {
                try {
                    //param.setFuturedays(verificationdays);
                    param.setFuturedays(0);
                    param.setOffset(0);
                    param.setDates(null, null, action, market);
                } catch (ParseException e1) {
                    log.error(Constants.EXCEPTION, e1);
                }            
                new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold(), param, null, market);
            }
        } catch (Exception e3) {
            log.error(Constants.EXCEPTION, e3);
        }
        incProp = incProp1;
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        new MarketUtil().filterIncDecs(param, market, profitdata, maps, true, null);
        new MarketUtil().filterIncDecs(param, market, profitdata, maps, false, null);

        double memoryFitness = 0.0;
        double incdecFitness = 0.0;
        try {
            if (true) {
                int fitnesses = 0;
                double fitness = 0;
                long countDec = 0;
                long sizeDec = 0;
                if (buy == null || buy == false) {
                    List<Boolean> listDecBoolean = listDec.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
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
                    List<Boolean> listIncBoolean = listInc.stream().map(IncDecItem::getVerified).filter(Objects::nonNull).collect(Collectors.toList());
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
                log.info("Fit #{} {}", this.hashCode(), ((MarketFilterChromosome2) chromosome).getGene().getMarketfilter());
            }
            //memoryItems = new MyFactory().myfactory(getConf(), PipelineConstants.MLMACD);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        double fitness = 0;
        memoryItems = myData.getMemoryItems();
        for (MemoryItem memoryItem : memoryItems) {
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

    @Override
    public double fitness(AbstractChromosome chromosome) {
        market.setFilter(((MarketFilterChromosome2) chromosome).getGene().getMarketfilter());
        
        return new FitnessMarketFilterCommon().fitnessCommon(action, param, market, profitdata, buy, stockDates, incdecs, parameters, componentName, map);
    }

}
