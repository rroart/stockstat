package roart.iclij.evolution.chromosome.impl;

import java.io.IOException;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;

public class PredictorChromosome extends ConfigMapChromosome2 {

    public PredictorChromosome() {
        super();
    }

    public PredictorChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    public double getFitness()
            throws StreamReadException, DatabindException, IOException {
        //((TensorflowPredictorLSTMConfig) config.getConfig()).full = true;
        //Map<String, Object> map = new HashMap<>();
        //String string = JsonUtil.convert(config);
        //map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG, string);
        //param.getService().conf.getConfigValueMap().putAll(map);
        //setMap(map);
        return super.getFitness();
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        PredictorChromosome chromosome = new PredictorChromosome(gene);
        for (int conf = 0; conf < getConfList().size(); conf++) {
            String confName = getConfList().get(conf);
            if (random.nextBoolean()) {
                chromosome.getMap().put(confName, this.getMap().get(confName));
            } else {
                chromosome.getMap().put(confName, ((ConfigMapChromosome2) other).getMap().get(confName));
            }
        }
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        return new Individual(chromosome);
    }
    
    @Override
    public double getEvaluations(int j) throws StreamReadException, DatabindException, IOException {
        return 0;
    }

    @Override
    public void transformToNode() throws StreamReadException, DatabindException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws StreamReadException, DatabindException, IOException {
    }

    @Override
    public AbstractChromosome copy() {
        PredictorChromosome chromosome = new PredictorChromosome(gene);
        chromosome.gene = gene.copy();
        //chromosome.config = new TensorflowLSTMConfig(config.getEpochs(), config.getWindowsize(), config.getHorizon());
        //chromosome.config = (TensorflowPredictorLSTMConfigGene) config.copy();
        return chromosome;
    }
    
}
