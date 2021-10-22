package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.genetics.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.action.MarketActionData;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class FitnessIclijConfigMap3 {

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
    
    public FitnessIclijConfigMap3(MarketActionData action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, IclijConfigMapGene gene, List<String> stockDates) {
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

    //@Override
    public double fitness(Chromosome chromosome) {
        Double score = fitness(profitdata, chromosome);
        IclijConfigMapGene gene = ((IclijConfigMapChromosome3) chromosome).getGene();
        param.getUpdateMap().putAll(gene.getMap());
        log.info("Fit {}", score);
        log.info("Fit #{} {} {}", this.hashCode(), gene.getMap(), this.toString());
        return score != null ? score : 0.0;
    }

    public Double fitness(ProfitData profitdata, Chromosome chromosome) {
        IclijConfigMapGene gene = ((IclijConfigMapChromosome3) chromosome).getGene();
        Map<String, Object> map = gene.getMap();

        List<String> titletexts = new ArrayList<>();
        Double fitness = new FitnessIclijConfigMapCommon().fitnessCommon(profitdata, map, action, market, param, componentName, subcomponent, parameters, titletexts, null);
        if (!titletexts.isEmpty()) {
            titletext = titletexts.get(0);
        }
        return fitness;
    }

}
