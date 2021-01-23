package roart.component;

import java.util.List;
import java.util.Map;

import roart.action.MarketAction;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.iclij.evolution.chromosome.winner.IclijConfigMapChromosomeWinner;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapChromosome;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.evolution.fitness.impl.FitnessIclijConfigMap;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class EvolveIclijConfigMapMy extends EvolveMy {

    @Override
    public ComponentData evolve(MarketAction action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsItem> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList) {
        SimulateInvestData param2 = (SimulateInvestData) param;
        List<String> stockDates = param2.getStockDates();
        IclijConfigMapGene gene = new IclijConfigMapGene(confList, param.getInput().getConfig());
        IclijConfigMapChromosome chromosome = new IclijConfigMapChromosome(gene);
        //loadme(param, chromosome, market, confList, buy, subcomponent, action, parameters);
        FitnessIclijConfigMap fit = new FitnessIclijConfigMap(action, param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        ComponentData i = component.improve(action, param, chromosome, subcomponent, new IclijConfigMapChromosomeWinner(), buy, fit);
        return i;
    }

}
