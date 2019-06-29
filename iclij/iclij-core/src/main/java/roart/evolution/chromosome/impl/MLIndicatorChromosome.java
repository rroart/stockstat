package roart.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.service.model.ProfitData;

public class MLIndicatorChromosome extends ConfigMapChromosome {

    public MLIndicatorChromosome(List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component, Boolean buy) {
        super(confList, param, profitdata, market, positions, component, buy);
    }

    @Override
    public boolean validate() {
        for (String key : getList()) {
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
        Random rand = new Random();
        int index = rand.nextInt(getList().size());
        getMap().put(getList().get(index), true);
    }

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATORRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASDELTAS);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASMACD);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASATR);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASCCI);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCH);
        list.add(ConfigConstants.AGGREGATORSINDICATOREXTRASSTOCHRSI);
        list.add(ConfigConstants.AGGREGATORSINDICATORINTERVALDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORFUTUREDAYS);
        list.add(ConfigConstants.AGGREGATORSINDICATORTHRESHOLD);
        return list;
    }

    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(confList, newparam, profitdata, market, positions, componentName, buy);
        return chromosome;
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(confList, newparam, profitdata, market, positions, componentName, buy);
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
