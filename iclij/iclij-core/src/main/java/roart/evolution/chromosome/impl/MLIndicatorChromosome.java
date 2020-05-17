package roart.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.component.Memories;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class MLIndicatorChromosome extends ConfigMapChromosome {

    public MLIndicatorChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String component, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        super(action, param, profitdata, market, positions, component, buy, subcomponent, parameters, gene, mlTests);
    }

    @Override
    public boolean validate() {
        for (String key : getValidateList()) {
            Object object = getMap().get(key);
            if (object != null && object instanceof Boolean) {
                if ((boolean) object) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void fixValidation() { 
        int index = random.nextInt(getValidateList().size());
        getMap().put(getValidateList().get(index), true);
    }

    private List<String> getValidateList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATORATR);
        list.add(ConfigConstants.AGGREGATORSINDICATORCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATORSTOCHRSI);
        list.retainAll(getConfList());
        return list;
    }

    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
	chromosome.getMap().putAll(getMap());
        return chromosome;
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
        for (int conf = 0; conf < getConfList().size(); conf++) {
            String confName = getConfList().get(conf);
            if (random.nextBoolean()) {
                chromosome.getMap().put(confName, this.getMap().get(confName));
            } else {
                chromosome.getMap().put(confName, ((ConfigMapChromosome) other).getMap().get(confName));
            }
        }
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        return new Individual(chromosome);
    }

}
