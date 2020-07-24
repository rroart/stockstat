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
    
    protected List<String> stockDates;
    
    public FitnessConfigMap(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<String> stockDates) {
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
            Component component =  action.getComponentFactory().factory(componentName);
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

            ComponentData componentData2 = component.handle(action, market, param, profitdata, listMap, evolve, gene.getMap(), subcomponent, null, parameters);
            Object[] result = component.calculateAccuracy(componentData2);
            score = (Double) result[0];
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return score;
    }

}
