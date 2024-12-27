package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.model.MemoryItem;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.Fitness;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.component.Component;
import roart.iclij.component.factory.ComponentFactory;
import roart.iclij.config.Market;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FitnessConfigMap extends Fitness {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected MarketActionData action;
    
    protected ComponentData param;

    protected ProfitData profitdata;

    protected Market market;

    protected Memories positions;

    protected String componentName;

    protected String subcomponent;

    protected Boolean buy;

    protected Parameters parameters;
    
    protected List<String> stockDates;
    
    protected String titletext;
    
    public FitnessConfigMap(MarketActionData action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<String> stockDates) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.positions = positions;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
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

        profitdata.setBuys(new HashMap<>());
        profitdata.setSells(new HashMap<>());
        Double score = fitness(myData, profitdata, chromosome);
        log.info("Fit {}", score);
        log.info("Fit #{} {}", this.hashCode(), this.toString());
        ConfigMapGene gene = ((ConfigMapChromosome2) chromosome).getGene();
        param.getUpdateMap().putAll(gene.getMap());
        return score != null ? score : 0.0;
    }

    public Double fitness(WebData myData, ProfitData profitdata, AbstractChromosome chromosome) {
        ConfigMapGene gene = ((ConfigMapChromosome2) chromosome).getGene();
        Double score = null;
        try {
            Component component =  new ComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            myData.setProfitData(profitdata);

            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            gene.getMap().put(ConfigConstants.MACHINELEARNINGMLCROSS, false);
            gene.getMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
            gene.getMap().put(ConfigConstants.MISCMYDAYS, 0);

            String key = component.getThreshold();
            gene.getMap().put(key, "[" + parameters.getThreshold() + "]");

            gene.getMap().put(ConfigConstants.MISCTHRESHOLD, null);
            
            Memories listMap = new Memories(market);

            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());

            ComponentData componentData2 = component.handle(action, market, param, profitdata, listMap, evolve, gene.getMap(), subcomponent, null, parameters, false);
            Object[] result = component.calculateAccuracy(componentData2);
            score = (Double) result[0];
            titletext = (String) componentData2.getUpdateMap().get(EvolveConstants.TITLETEXT);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return score;
    }

}
