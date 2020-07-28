package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.MyMyConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.AbstractGene;
import roart.gene.impl.ConfigMapGene;
import roart.gene.ml.impl.TensorflowPredictorLSTMConfigGene;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.component.ComponentInput;

public class PredictorChromosome extends ConfigMapChromosome2 {

    public PredictorChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
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
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
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
