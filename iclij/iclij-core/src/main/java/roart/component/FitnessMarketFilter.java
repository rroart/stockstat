package roart.component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.Fitness;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class FitnessMarketFilter extends Fitness {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Object> map = new HashMap<>();

    private MarketAction action;
    
    private Market market;
    
    private ComponentData param;
    
    private ProfitData profitdata;

    protected Boolean buy;

    protected String componentName;
    
    private String subcomponent;
    
    private Parameters parameters;
    
    private List<MLMetricsItem> mlTests;

    public FitnessMarketFilter(MarketAction action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
        this.mlTests = mlTests;
    }

    @Override
    public double fitness(AbstractChromosome chromosome) {
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
        List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
        List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
        List<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec);
        Trend incProp = null;
        incProp = extracted(chromosome, myData, listInc, listDec, mlTests);
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        action.filterIncDecs(param, market, profitdata, maps, true);
        action.filterIncDecs(param, market, profitdata, maps, false);

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
                int minimum = param.getInput().getConfig().getImproveProfitFitnessMinimum();
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

    public Trend extracted(AbstractChromosome chromosome, WebData myData, List<IncDecItem> listInc, List<IncDecItem> listDec, List<MLMetricsItem> mlTests) {
        Trend incProp = null;
        try {
            int verificationdays = param.getInput().getConfig().verificationDays();
            Component component =  action.getComponentFactory().factory(componentName);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            //ProfitData profitdata = new ProfitData();
            myData.setProfitData(profitdata);
            boolean myevolve = component.wantImproveEvolve();
            if (!param.getService().conf.wantIndicatorRecommender()) {
                int jj = 0;
            }
            map.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            map.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            map.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);

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
            market.setFilter(((MarketFilterChromosome2)chromosome).getGene().getMarketfilter());
            ComponentData componentData = component.handle(action, market, param, profitdata, new Memories(market), myevolve /*evolve && evolvefirst*/, map, subcomponent, null, parameters);
            //componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData, parameters);
                if (memories == null || memories.isEmpty()) {
                    int jj = 0;
                }
                myData.getMemoryItems().addAll(memories);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            Memories listMap = new Memories(market);
            listMap.method(myData.getMemoryItems(), param.getInput().getConfig());        
            //ProfitData profitdata = new ProfitData();
            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());
            Memories positions = listMap;

            //component.enableDisable(componentData, positions, param.getConfigValueMap(), buy);

            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, map, subcomponent, null, parameters);
            component.calculateIncDec(componentData2, profitdata, positions, buy, mlTests, parameters);

            short startoffset = new MarketUtil().getStartoffset(market);
            action.setValMap(param);
            VerifyProfit verify = new VerifyProfit();
            incProp = verify.getTrend(verificationdays, param.getCategoryValueMap(), startoffset);
            //Trend incProp = new FindProfitAction().getTrend(verificationdays, param.getFutureDate(), param.getService());
            //log.info("trendcomp {} {}", trend, incProp);
            if (verificationdays > 0) {
                try {
                    //param.setFuturedays(verificationdays);
                    param.setFuturedays(0);
                    param.setOffset(0);
                    param.setDates(null, null, action.getActionData(), market);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }            
                new VerifyProfitUtil().getVerifyProfit(verificationdays, null, null, listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold(), param, null, market);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incProp;
    }

}
