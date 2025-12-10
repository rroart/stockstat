package roart.iclij.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class")
@JsonSubTypes({  
    @Type(value = MLATRChromosome.class, name = "MLATRChromosome"),
    @Type(value = MLCCIChromosome.class, name = "MLCCIChromosome"),
    @Type(value = MLMACDChromosome.class, name = "MLMACDChromosome"),
    @Type(value = MLMultiChromosome.class, name = "MLMultiChromosome"),
    @Type(value = MLRSIChromosome.class, name = "MLRSIChromosome"),
    @Type(value = MLSTOCHChromosome.class, name = "MLSTOCHChromosome")
})
public abstract class MLAggregatorChromosome extends ConfigMapChromosome2 {
    public MLAggregatorChromosome() {
        super();
    }

    public MLAggregatorChromosome(ConfigMapGene gene) {
        super(gene);
    }

    protected abstract MLAggregatorChromosome getNewChromosome();

    @Override
    public boolean validate() {
        boolean foundbool = false;
        for (String key : getValidateList()) {
            Object object = getMap().get(key);
            if (object != null && object instanceof Boolean) {
                foundbool = true;
                if ((boolean) object) {
                    return true;
                }
            }
        }
        return !foundbool;
    }
    
    @Override
    public void fixValidation() { 
        if (getValidateList().isEmpty()) {
            return;
        }
        int index = random.nextInt(getValidateList().size());
        getMap().put(getValidateList().get(index), true);
    }

    @Override
    public double getFitness()
            throws StreamReadException, DatabindException, IOException {
        return super.getFitness();
    }

    protected List<String> getValidateList() {
        List<String> confList = new ArrayList<>();
        return confList;
    }
    
    @Override
    public AbstractChromosome copy() {
        MLAggregatorChromosome chromosome = getNewChromosome();
        chromosome.gene = gene.copy();
        return chromosome;
    }

    @Override
    public Individual crossover(AbstractChromosome other) {
        MLAggregatorChromosome chromosome = getNewChromosome();
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

}
