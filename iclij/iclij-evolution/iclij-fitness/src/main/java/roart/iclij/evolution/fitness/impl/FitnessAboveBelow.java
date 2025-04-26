package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.Market;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.action.MarketActionData;
import roart.service.model.ProfitData;

public class FitnessAboveBelow extends Fitness {

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

    private List<IncDecDTO> incdecs;
    
    private List<String> components = new ArrayList<>();
    
    private List<String> subcomponents = new ArrayList<>();

    private List<String> stockDates;

    public FitnessAboveBelow(MarketActionData action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, List<IncDecDTO> incdecs, List<String> components, List<String> subcomponents, List<String> stockDates) {
        this.action = action;
        this.param = param;
        this.profitdata = profitdata;
        this.market = market;
        this.componentName = componentName;
        this.subcomponent = subcomponent;
        this.buy = buy;
        this.parameters = parameters;
        this.mlTests = mlTests;
        this.incdecs = incdecs;
        this.components = components;
        this.subcomponents = subcomponents;
        this.stockDates = stockDates;
    }

    @Override
    public double fitness(AbstractChromosome chromosome) {
        AboveBelowChromosome my = (AboveBelowChromosome) chromosome;
        List<Boolean> genes = my.getGenes();
        
        return new FitnessAboveBelowCommon().fitnessCommon(genes, action, param, profitdata, components, subcomponents, incdecs, market, stockDates, parameters, buy, componentName, map);
    }

    @Override
    public String subTitleText() {
        List retList = new ArrayList<>();
        retList.add(components);
        retList.add(subcomponents);
        return JsonUtil.convert(retList);
    }
}
