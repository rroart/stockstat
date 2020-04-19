package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.util.MiscUtil;
import roart.iclij.util.VerifyProfit;
import roart.iclij.util.VerifyProfitUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = MLMACDChromosome.class, name = "roart.evolution.chromosome.impl.MLMACDChromosome") })  
public class ConfigMapChromosome extends AbstractChromosome {
    protected ConfigMapGene gene;
    
    protected MarketAction action;
    
    protected ComponentData param;

    protected ProfitData profitdata;

    protected Market market;

    protected List<Integer> positions;

    protected String componentName;

    protected String subcomponent;

    protected Boolean buy;

    protected Parameters parameters;
    
    protected List<MLMetricsItem> mlTests;
    
    public ConfigMapChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.positions = positions;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
        this.gene = gene;
        this.mlTests = mlTests;
    }

    public ConfigMapChromosome(ConfigMapChromosome chromosome) {
        this(chromosome.action, chromosome.param, chromosome.profitdata, chromosome.market, chromosome.positions, chromosome.componentName, chromosome.buy, chromosome.subcomponent, chromosome.parameters, chromosome.gene, chromosome.mlTests);
    }

    public ConfigMapGene getGene() {
        return gene;
    }

    public void setGene(ConfigMapGene gene) {
        this.gene = gene;
    }

    public Map<String, Object> getMap() {
        return gene.getMap();
    }

    public void setMap(Map<String, Object> map) {
        gene.setMap(map);
    }

    public List<String> getConfList() {
        return gene.getConfList();
    }

    public void setConfList(List<String> confList) {
        gene.setConfList(confList);
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    @Override
    public void mutate() {
        gene.mutate();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        gene.randomize();
    }


    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        List<MemoryItem> memoryItems = null;
        WebData myData = new WebData();
        myData.setUpdateMap(new HashMap<>());
        myData.setMemoryItems(new ArrayList<>());
        //myData.profitData = new ProfitData();
        myData.setTimingMap(new HashMap<>());
        int b = param.getService().conf.hashCode();
        boolean c = param.getService().conf.wantIndicatorRecommender();
        List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
        List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
        List<IncDecItem> listIncDec = new MiscUtil().moveAndGetCommon(listInc, listDec);
        Trend incProp = null;
        incProp = extracted(myData, listInc, listDec);

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
        param.getUpdateMap().putAll(getMap());
        return incdecFitness;
    }

    public Trend extracted(WebData myData, List<IncDecItem> listInc, List<IncDecItem> listDec) {
        Trend incProp = null;
        try {
            int verificationdays = param.getInput().getConfig().verificationDays();
            boolean evolvefirst = new MiscUtil().getEvolve(verificationdays, param.getInput());
            Component component =  action.getComponentFactory().factory(componentName);
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

            String key = component.getThreshold();
            gene.getMap().put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            gene.getMap().put(key2, parameters.getFuturedays());

            gene.getMap().put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(action, market, param, profitdata, new ArrayList<>(), myevolve /*evolve && evolvefirst*/, gene.getMap(), subcomponent, null, parameters);
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

            Map<Pair<String, Integer>, List<MemoryItem>> listMap = new HashMap<>();
            myData.getMemoryItems().forEach(m -> new ImproveProfitAction().listGetterAdder(listMap, new ImmutablePair<String, Integer>(m.getComponent(), m.getPosition()), m));
            ProfitInputData inputdata = new ImproveProfitAction().filterMemoryListMapsWithConfidence(market, listMap, param.getInput().getConfig());        
            //ProfitData profitdata = new ProfitData();
            profitdata.setInputdata(inputdata);
            Map<String, List<Integer>> listComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getListMap());
            /*
            Map<String, List<Integer>> aboveListComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getAboveListMap());
            Map<String, List<Integer>> belowListComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getBelowListMap());
            Map<Boolean, Map<String, List<Integer>>> listComponentMap = new HashMap<>();
            listComponentMap.put(null, listComponent);
            listComponentMap.put(true, aboveListComponent);
            listComponentMap.put(false, belowListComponent);
            */
            inputdata.setNameMap(new HashMap<>());
            List<Integer> positions = listComponent.get(componentName);

            component.enableDisable(componentData, positions, param.getConfigValueMap());

            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, gene.getMap(), subcomponent, null, parameters);
            component.calculateIncDec(componentData2, profitdata, positions, buy, mlTests);

            Short mystartoffset = market.getConfig().getStartoffset();
            short startoffset = mystartoffset != null ? mystartoffset : 0;
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
                    param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
                } catch (ParseException e) {
                    log.error(Constants.EXCEPTION, e);
                }            
                new VerifyProfitUtil().getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incProp;
    }

    private void configSaves(ComponentData param, Map<String, Object> anUpdateMap) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(componentName);
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

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        ConfigMapGene newNNConfig =  (ConfigMapGene) gene.crossover(((ConfigMapChromosome) chromosome).gene);
        ConfigMapChromosome eval = new ConfigMapChromosome(action, param, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        ConfigMapChromosome chromosome = new ConfigMapChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
        chromosome.gene = gene.copy();
        return chromosome;
    }

    @Override
    public boolean isEmpty() {
        return gene.isEmpty();
    }

    @Override
    public String toString() {
        return gene.toString();
    }

}
