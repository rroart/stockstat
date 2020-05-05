package roart.component;

import java.util.Map;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.marketfilter.chromosome.impl.MarketFilterChromosome;
import roart.evolution.species.Individual;
import roart.iclij.config.MarketFilter;

public class MarketFilterChromosomeWinner extends ChromosomeWinner {

    @Override
    public double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap) {
        MarketFilterChromosome bestChromosome = (MarketFilterChromosome) best.getEvaluation();
        MarketFilter aConf = bestChromosome.getGene().getMarketfilter();
        confMap.put("some", JsonUtil.convert(aConf));
        param.setUpdateMap(confMap);
        double score = best.getFitness();
        return score;
    }

}
