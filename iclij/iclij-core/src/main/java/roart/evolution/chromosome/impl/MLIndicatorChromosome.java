package roart.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.service.model.ProfitData;

public class MLIndicatorChromosome extends ConfigMapChromosome {

    public MLIndicatorChromosome(List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component) {
        super(confList, param, profitdata, market, positions, component);
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
        return list;
    }
    
    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(confList, newparam, profitdata, market, positions, componentName);
        return chromosome;
    }
    

}
