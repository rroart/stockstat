package roart.iclij.evolution.chromosome.winner;

import java.util.Map;

import roart.component.model.ComponentData;
import roart.evolution.species.Individual;

public abstract class ChromosomeWinner {

    public abstract double handleWinner(ComponentData param, Individual best, Map<String, Object> confMap);
}
