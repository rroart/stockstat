package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.Action;
import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.evolution.chromosome.impl.IclijConfigMapChromosome;
import roart.evolution.chromosome.impl.IclijConfigMapGene;
import roart.evolution.fitness.Fitness;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.factory.actioncomponentconfig.ActionComponentConfigFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.config.ActionComponentConfig;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FitnessIclijConfigMap extends Fitness {

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
    
    protected List<String> stockDates;

    public FitnessIclijConfigMap(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, IclijConfigMapGene gene, List<String> stockDates) {
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
        IclijConfigMapGene gene = ((IclijConfigMapChromosome) chromosome).getGene();
        param.getUpdateMap().putAll(gene.getMap());
        log.info("Fit {}", score);
        log.info("Fit #{} {} {}", this.hashCode(), gene.getMap(), this.toString());
        return score != null ? score : 0.0;
    }

    public Double fitness(WebData myData, ProfitData profitdata, AbstractChromosome chromosome) {
        IclijConfigMapGene gene = ((IclijConfigMapChromosome) chromosome).getGene();
        Double score = null;
        try {
            Component component =  action.getComponentFactory().factory(componentName);
            ActionComponentConfig config = ActionComponentConfigFactory.factoryfactory(action.getName()).factory(component.getPipeline());
            component.setConfig(config);
            boolean evolve = false; // component.wantEvolve(param.getInput().getConfig());
            myData.setProfitData(profitdata);

            Memories listMap = new Memories(market);

            ProfitInputData inputdata = new ProfitInputData();
            profitdata.setInputdata(inputdata);
            inputdata.setNameMap(new HashMap<>());

            param.getInput().getConfig().getConfigValueMap().putAll(gene.getMap());
            
            Action parent = action.getParent();
            action.setParent(null);
            ComponentData componentData2 = component.handle(action, market, param, profitdata, listMap, evolve, gene.getMap(), subcomponent, null, parameters);
            action.setParent(parent);
            Object[] result = component.calculateAccuracy(componentData2);
            score = (Double) result[0];
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return score;
    }

    @Override
    public String titleText() {
        return componentName;
    }
}
