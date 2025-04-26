package roart.iclij.evolve;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomKeyMutation;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;

import roart.common.model.MLMetricsDTO;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.Market;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public abstract class EvolveA extends Evolve {
    
    /*
    public abstract double evolve(MarketActionData action, ComponentData param, Market market, ProfitData profitdata, Boolean buy,
            String subcomponent, Parameters parameters, List<MLMetricsDTO> mlTests, Map<String, Object> confMap,
            EvolutionConfig evolutionConfig, String pipeline, Component component, List<String> confList);
    */
}
