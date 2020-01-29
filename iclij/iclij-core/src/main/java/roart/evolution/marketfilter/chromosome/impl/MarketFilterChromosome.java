package roart.evolution.marketfilter.chromosome.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MarketAction;
import roart.action.VerifyProfit;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.marketfilter.gene.impl.MarketFilterGene;
import roart.evolution.species.Individual;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class MarketFilterChromosome extends AbstractChromosome {
    private MarketFilterGene gene;

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

    protected Parameters parameters;
    
    public MarketFilterChromosome(MarketAction action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String componentName, Boolean buy, String subcomponent, Parameters parameters) {
        this.action = action;
        this.confList = confList;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.positions = positions;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
    }

    public MarketFilterChromosome(MarketFilterChromosome marketFilterChromosome) {
        // TODO Auto-generated constructor stub
    }

    public MarketFilterGene getGene() {
        return gene;
    }

    public void setGene(MarketFilterGene gene) {
        this.gene = gene;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public MarketAction getAction() {
        return action;
    }

    public void setAction(MarketAction action) {
        this.action = action;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    public ComponentData getParam() {
        return param;
    }

    public void setParam(ComponentData param) {
        this.param = param;
    }

    public ProfitData getProfitdata() {
        return profitdata;
    }

    public void setProfitdata(ProfitData profitdata) {
        this.profitdata = profitdata;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
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
        int b = param.getService().conf.hashCode();
        boolean c = param.getService().conf.wantIndicatorRecommender();
        List<IncDecItem> listInc = new ArrayList<>(profitdata.getBuys().values());
        List<IncDecItem> listDec = new ArrayList<>(profitdata.getSells().values());
        List<IncDecItem> listIncDec = ServiceUtil.moveAndGetCommon(listInc, listDec);
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

    public Trend extracted(WebData myData, List<IncDecItem> listInc, List<IncDecItem> listDec) {
        Trend incProp = null;
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

            String key = component.getThreshold();
            map.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            map.put(key2, parameters.getFuturedays());

            map.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(action, market, param, profitdata, new ArrayList<>(), myevolve /*evolve && evolvefirst*/, map, subcomponent, null, parameters);
            //componentData.setUsedsec(time0);
            myData.updateMap.putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData, parameters);
                if (memories == null || memories.isEmpty()) {
                    int jj = 0;
                }
                myData.memoryItems.addAll(memories);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            Map<Pair<String, Integer>, List<MemoryItem>> listMap = new HashMap<>();
            myData.memoryItems.forEach(m -> new ImproveProfitAction().listGetterAdder(listMap, new ImmutablePair<String, Integer>(m.getComponent(), m.getPosition()), m));
            ProfitInputData inputdata = new ImproveProfitAction().filterMemoryListMapsWithConfidence(market, listMap);        
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

            ComponentData componentData2 = component.handle(action, market, param, profitdata, positions, evolve, map, subcomponent, null, parameters);
            component.calculateIncDec(componentData2, profitdata, positions, buy);

            Short mystartoffset = market.getConfig().getStartoffset();
            short startoffset = mystartoffset != null ? mystartoffset : 0;
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
                new FindProfitAction().getVerifyProfit(verificationdays, param.getFutureDate(), param.getService(), param.getBaseDate(), listInc, listDec, new ArrayList<>(), startoffset, parameters.getThreshold());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incProp;
    }

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        MarketFilterGene newNNConfig =  (MarketFilterGene) gene.crossover(((MarketFilterChromosome) chromosome).gene);
        MarketFilterChromosome eval = new MarketFilterChromosome(action, confList, param, profitdata, market, positions, componentName, buy, subcomponent, parameters);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new MarketFilterChromosome(this);
    }

    @Override
    public boolean isEmpty() {
        return gene == null;
    }
    
    @Override
    public String toString() {
        return "" + gene;
    }

}
