package roart.component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.action.ImproveProfitAction;
import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.evolution.fitness.Fitness;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
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

public class FitnessConfigMap extends Fitness {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected MarketAction action;
    
    protected ComponentData param;

    protected ProfitData profitdata;

    protected Market market;

    protected Memories positions;

    protected String componentName;

    protected String subcomponent;

    protected Boolean buy;

    protected Parameters parameters;
    
    @Deprecated
    protected List<MLMetricsItem> mlTests;
    
    protected List<String> stockDates;
    
    public FitnessConfigMap(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests, List<String> stockDates) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.positions = positions;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
        this.mlTests = mlTests;
        this.stockDates = stockDates;
    }

    @Override
    public double fitness(AbstractChromosome chromosome) {
        List<MemoryItem> memoryItems = null;
        WebData myData = new WebData();
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryItems(new ArrayList<>());
        //myData.profitData = new ProfitData();
        myData.setTimingMap(new HashMap<>());
        int b = param.getService().conf.hashCode();
        boolean c = param.getService().conf.wantIndicatorRecommender();

        profitdata.setBuys(new HashMap<>());
        profitdata.setSells(new HashMap<>());
        Trend incProp = null;
        incProp = extracted(myData, profitdata, chromosome);
        List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
        List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
        List<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec);

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
                log.info("Fit #{} {}", this.hashCode(), this.toString());
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
        ConfigMapGene gene = ((ConfigMapChromosome2) chromosome).getGene();
        param.getUpdateMap().putAll(gene.getMap());
        return incdecFitness;
    }

    public Trend extracted(WebData myData, ProfitData profitdata, AbstractChromosome chromosome) {
        ConfigMapGene gene = ((ConfigMapChromosome2) chromosome).getGene();
        Trend incProp = null;
        try {
            Component component =  action.getComponentFactory().factory(componentName);
            String futureDaysKey = component.getFuturedays();
            int verificationdays = (int) gene.getMap().get(futureDaysKey);
     
            String date = TimeUtil.convertDate2(param.getFutureDate());
            try {
                //param.setFuturedays(verificationdays);
                param.setFuturedays(verificationdays);
                param.setOffset(0);
                param.setDates(date, stockDates, action.getActionData(), market);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            } 

            //param.getInput().getConfig().verificationDays();
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            //ProfitData profitdata = new ProfitData();
            myData.setProfitData(profitdata);
            boolean myevolve = component.wantImproveEvolve();
            if (!param.getService().conf.wantIndicatorRecommender()) {
                int jj = 0;
            }
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLCROSS, false);
            gene.getMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
            gene.getMap().put(ConfigConstants.MISCMYDAYS, 0);

            String key = component.getThreshold();
            gene.getMap().put(key, "[" + parameters.getThreshold() + "]");
            //String key2 = component.getFuturedays();
            //gene.getMap().put(key2, parameters.getFuturedays());

            gene.getMap().put(ConfigConstants.MISCTHRESHOLD, null);
            
            Memories listMap = new Memories(market);

            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());

            ComponentData componentData2 = component.handle(action, market, param, profitdata, listMap, evolve, gene.getMap(), subcomponent, null, parameters);
            component.calculateIncDec(componentData2, profitdata, listMap, buy, null, parameters);

            short startoffset = new MarketUtil().getStartoffset(market);
            //action.setValMap(param);
            VerifyProfit verify = new VerifyProfit();
            incProp = verify.getTrend(verificationdays, param.getCategoryValueMap(), startoffset);
            //Trend incProp = new FindProfitAction().getTrend(verificationdays, param.getFutureDate(), param.getService());
            //log.info("trendcomp {} {}", trend, incProp);
            if (verificationdays > 0) {
                /*
                try {
                    //param.setFuturedays(verificationdays);
                    param.setFuturedays(0);
                    param.setOffset(0);
                    param.setDates(null, null, action.getActionData(), market);
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                } 
                */           
                List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
                List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
                new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold(), stockDates, param.getCategoryValueMap());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incProp;
    }

}
