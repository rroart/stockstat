package roart.iclij.evolution.fitness.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.action.MarketActionData;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapGene;
import roart.evolution.iclijconfigmap.jenetics.gene.impl.IclijConfigMapChromosome;

public class FitnessIclijConfigMap2 {
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
    
    public FitnessIclijConfigMap2(MarketActionData action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String componentName, Boolean buy, String subcomponent, Parameters parameters, IclijConfigMapGene gene, List<String> stockDates) {
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

    public synchronized double fitness2(IclijConfigMapGene chromosome) {
        return 0;
    }
    
    public synchronized double fitness(IclijConfigMapChromosome chromosome) {
        IclijConfigMapGene gene = ((IclijConfigMapChromosome) chromosome).getGene();
        Map<String, Object> map = gene.getMap();

        List<String> titletexts = new ArrayList<>();
        Double fitness = new FitnessIclijConfigMapCommon().fitnessCommon(profitdata, map, action, market, param, componentName, subcomponent, parameters, titletexts, null);
        if (!titletexts.isEmpty()) {
            titletext = titletexts.get(0);
        }
        Double score = fitness;
        IclijConfigMapGene gene2 = ((IclijConfigMapChromosome) chromosome).getGene();
        param.getUpdateMap().putAll(gene2.getMap());
        log.info("Fit {} #{} #{} #{}", score, chromosome.hashCode(), gene.hashCode(), map.hashCode());
        log.info("Fit #{} {} {}", map.hashCode(), map, this.toString());
        return score;
    }

}
