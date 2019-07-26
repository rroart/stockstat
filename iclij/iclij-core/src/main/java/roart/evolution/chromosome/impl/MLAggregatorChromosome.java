package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.component.model.ComponentData;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.service.model.ProfitData;

public abstract class MLAggregatorChromosome extends ConfigMapChromosome {
    public MLAggregatorChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy) {
        super(confList, param, profitdata, market, positions, component, buy);
    }

    protected abstract MLAggregatorChromosome getNewChromosome(ComponentData newparam);

    @Override
    public boolean validate() {
        boolean foundbool = false;
        for (String key : getValidateList()) {
            Object object = getMap().get(key);
            if (object != null && object instanceof Boolean) {
                foundbool = true;
                if ((boolean) object) {
                    return true;
                }
            }
        }
        return !foundbool;
    }
    
    @Override
    public void fixValidation() { 
        if (getValidateList().isEmpty()) {
            return;
        }
        Random rand = new Random();
        int index = rand.nextInt(getValidateList().size());
        getMap().put(getValidateList().get(index), true);
    }

    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        return super.getFitness();
    }

    protected List<String> getValidateList() {
        List<String> confList = new ArrayList<>();
        return confList;
    }
    
    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        MLAggregatorChromosome chromosome = getNewChromosome(newparam);
        chromosome.getMap().putAll(getMap());
        return chromosome;
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        ComponentData newparam = new ComponentData(param);
        MLAggregatorChromosome chromosome = getNewChromosome(newparam);
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (rand.nextBoolean()) {
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
