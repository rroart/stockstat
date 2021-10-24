package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import roart.iclij.model.action.MarketActionData;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class FitnessAboveBelow2 {
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
    
    private List<MLMetricsItem> mlTests;

    private List<IncDecItem> incdecs;
    
    private List<String> components = new ArrayList<>();
    
    private List<String> subcomponents = new ArrayList<>();

    private List<String> stockDates;

    public FitnessAboveBelow2(MarketActionData action, List<String> confList, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, List<IncDecItem> incdecs, List<String> components, List<String> subcomponents, List<String> stockDates) {
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

    public synchronized Double fitness3(Genotype<BitGene> gt) {
        List<Boolean> genes = new ArrayList<>();
        Chromosome<BitGene> c = gt.getChromosome();
        int len = c.length();
        for (int i = 0; i < len; i++) {
            Boolean b = c.getGene(i).getAllele();
            genes.add(b);
        }
        return new FitnessAboveBelowCommon().fitnessCommon(genes, action, param, profitdata, components, subcomponents, incdecs, market, stockDates, parameters, buy, componentName, map);
    }
    public synchronized Double fitness2(BitChromosome chromosome) {
        return null;
    }
    public synchronized Integer fitness1(BitChromosome chromosome) {
        return null;
    }
    public synchronized BitGene fitness(BitChromosome chromosome) {
        return null;
    }
}
