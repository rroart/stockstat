package roart.evolution.chromosome.winner;

import java.util.Map;

import roart.component.model.ComponentData;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.species.Individual;

public class IclijConfigMapChromosomeWinner extends ChromosomeWinner {

    @Override
    public double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap) {
        IclijConfigMapChromosome bestChromosome = (IclijConfigMapChromosome) best.getEvaluation();
        confMap.putAll(bestChromosome.getMap());
        param.setUpdateMap(confMap);
        double score = best.getFitness();
        return score;
    }

}
