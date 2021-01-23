package roart.iclij.evolution.chromosome.winner;

import java.util.List;
import java.util.Map;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.species.Individual;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;

public class AboveBelowChromosomeWinner extends ChromosomeWinner {

    private String parameters;
    
    private List<String> compsub;
    
    public AboveBelowChromosomeWinner(String parameters, List<String> compsub) {
        this.parameters = parameters;
        this.compsub = compsub;
    }

    @Override
    public double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap) {
        AboveBelowChromosome bestChromosome = (AboveBelowChromosome) best.getEvaluation();
        List<Boolean> aConf = bestChromosome.getGenes();
        String conf = "";
        for (int i1 = 0; i1 < compsub.size(); i1++) {
            Boolean b = aConf.get(i1);
            if (b) {
                conf = conf + compsub.get(i1) + ", ";
            }
        }
        confMap.put(parameters, conf);
        param.setUpdateMap(confMap);
        double score = best.getFitness();
        return score;
    }

}
