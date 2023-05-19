package roart.iclij.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;

public class MLIndicatorChromosome extends ConfigMapChromosome2 {

    public MLIndicatorChromosome(ConfigMapGene gene) {
        super(gene);
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
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(gene);
        chromosome.gene = gene.copy();
        return chromosome;
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(gene);
        for (int conf = 0; conf < getConfList().size(); conf++) {
            String confName = getConfList().get(conf);
            if (random.nextBoolean()) {
                chromosome.getMap().put(confName, this.getMap().get(confName));
            } else {
                chromosome.getMap().put(confName, ((ConfigMapChromosome2) other).getMap().get(confName));
            }
        }
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        return new Individual(chromosome);
    }

}
