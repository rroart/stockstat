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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MarketAction;
import roart.action.VerifyProfit;
import roart.action.WebData;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.ImproveProfitComponentFactory;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Trend;
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
    private Map<String, Object> map = new HashMap<>();

    protected MarketAction action;
    
    protected List<String> confList;

    protected ComponentData param;

    protected ProfitData profitdata;

    protected Market market;

    protected List<Integer> positions;

    protected String componentName;

    protected String subcomponent;

    protected Boolean buy;
    
    public ConfigMapChromosome(MarketAction action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String componentName, Boolean buy, String subcomponent) {
        this.action = action;
        this.confList = confList;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.positions = positions;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            generateConfigNum(rand, conf);
        }
        if (!validate()) {
            fixValidation();
        }
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int conf = rand.nextInt(confList.size());
        generateConfigNum(rand, conf);
        if (!validate()) {
            fixValidation();
        }
    }

    private void generateConfigNum(Random rand, int conf) {
        String confName = confList.get(conf);
        Double[] range = this.param.getService().conf.getRange().get(confName);
        Class type = this.param.getService().conf.getType().get(confName);
        if (type == Boolean.class) {
            Boolean b = rand.nextBoolean();
            map.put(confName, b);
            return;
        }
        if (type == Integer.class) {
            Integer i = (range[0].intValue()) + rand.nextInt(range[1].intValue() - range[0].intValue());
            map.put(confName, i);
            return;
        }
        if (type == Double.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            map.put(confName, d);
            return;
        }
        log.error("Unknown type for {}", confName);
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        ConfigMapChromosome chromosome = new ConfigMapChromosome(action, confList, param, profitdata, market, positions, componentName, buy, subcomponent);
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (rand.nextBoolean()) {
                chromosome.map.put(confName, this.map.get(confName));
            } else {
                chromosome.map.put(confName, ((ConfigMapChromosome) other).map.get(confName));
            }
        }
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        return new Individual(chromosome);
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
        myData.updateMap = new HashMap<>();
        myData.memoryItems = new ArrayList<>();
        //myData.profitData = new ProfitData();
        myData.timingMap = new HashMap<>();
        double memoryFitness = 0.0;
        double incdecFitness = 0.0;
        int b = param.getService().conf.hashCode();
        boolean c = param.getService().conf.wantIndicatorRecommender();
        try {
            int verificationdays = param.getInput().getConfig().verificationDays();
            boolean evolvefirst = ServiceUtil.getEvolve(verificationdays, param);
            Component component =  action.getComponentFactory().factory(componentName);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            //ProfitData profitdata = new ProfitData();
            myData.profitData = profitdata;
            boolean myevolve = component.wantImproveEvolve();
            if (!param.getService().conf.wantIndicatorRecommender()) {
                int jj = 0;
            }
            map.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            map.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            map.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            ComponentData componentData = component.handle(action, market, param, profitdata, new ArrayList<>(), myevolve /*evolve && evolvefirst*/, map, subcomponent);
            //componentData.setUsedsec(time0);
            myData.updateMap.putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData);
                if (memories == null || memories.isEmpty()) {
                    int jj = 0;
                }
                myData.memoryItems.addAll(memories);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            Map<Object[], List<MemoryItem>> listMap = new HashMap<>();
            myData.memoryItems.forEach(m -> new ImproveProfitAction().listGetterAdder(listMap, new Object[]{m.getComponent(), m.getSubcomponent(), m.getPosition() }, m));
            ProfitInputData inputdata = new ImproveProfitAction().filterMemoryListMapsWithConfidence(market, listMap);        
            //ProfitData profitdata = new ProfitData();
            profitdata.setInputdata(inputdata);
            Map<String, List<Integer>> listComponent = new FindProfitAction().createComponentPositionListMap(inputdata.getListMap());
            inputdata.setNameMap(new HashMap<>());
            List<Integer> positions = listComponent.get(componentName);

            component.enableDisable(componentData, positions, param.getConfigValueMap());

            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, map, subcomponent);
            component.calculateIncDec(componentData2, profitdata, positions);

            List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
            List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
            List<IncDecItem> listIncDec = ServiceUtil.moveAndGetCommon(listInc, listDec);
            Short mystartoffset = market.getConfig().getStartoffset();
            short startoffset = mystartoffset != null ? mystartoffset : 0;
            VerifyProfit verify = new VerifyProfit();
            Trend incProp = verify.getTrend(verificationdays, param.getCategoryValueMap(), startoffset);
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
                new FindProfitAction().getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), listInc, listDec, startoffset);
            }

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
        memoryItems = myData.memoryItems;
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
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        ConfigMapChromosome chromosome = new ConfigMapChromosome(action, confList, newparam, profitdata, market, positions, componentName, buy, subcomponent);
        chromosome.map = new HashMap<>(this.map);
        return chromosome;
    }

    @Override
    public boolean isEmpty() {
        return confList == null || confList.isEmpty();
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
