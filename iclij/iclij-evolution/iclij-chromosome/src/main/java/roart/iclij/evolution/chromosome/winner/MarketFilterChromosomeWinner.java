package roart.iclij.evolution.chromosome.winner;

import java.util.Map;

import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.species.Individual;
import roart.iclij.config.MarketFilter;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;

public class MarketFilterChromosomeWinner extends ChromosomeWinner {

    @Override
    public double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap) {
        MarketFilterChromosome2 bestChromosome = (MarketFilterChromosome2) best.getEvaluation();
        MarketFilter aConf = bestChromosome.getGene().getMarketfilter();
        confMap.put("some", JsonUtil.convert(aConf));
        param.setUpdateMap(confMap);
        double score = best.getFitness();
        return score;
    }

}
