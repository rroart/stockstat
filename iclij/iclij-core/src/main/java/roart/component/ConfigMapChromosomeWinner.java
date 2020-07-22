package roart.component;

import java.util.Map;

import roart.component.model.ComponentData;
import roart.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.evolution.species.Individual;

public class ConfigMapChromosomeWinner extends ChromosomeWinner {

    @Override
    public double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap) {
        ConfigMapChromosome2 bestChromosome = (ConfigMapChromosome2) best.getEvaluation();
        confMap.putAll(bestChromosome.getMap());
        param.setUpdateMap(confMap);
        double score = best.getFitness();
        return score;
    }

}
