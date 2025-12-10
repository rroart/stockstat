package roart.iclij.evolution.marketfilter.chromosome.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene;
import roart.evolution.species.Individual;

public class MarketFilterChromosome2 extends AbstractChromosome {
    private MarketFilterGene gene;

    protected List<String> confList;

    public MarketFilterChromosome2(List<String> confList, MarketFilterGene gene) {
        this.confList = confList;
        this.gene = gene;
    }

    public MarketFilterChromosome2(MarketFilterChromosome2 marketFilterChromosome) {
        this(marketFilterChromosome.confList, marketFilterChromosome.getGene().copy());
    }

    public MarketFilterChromosome2() {
        // Json
    }

    public MarketFilterGene getGene() {
        return gene;
    }

    public void setGene(MarketFilterGene gene) {
        this.gene = gene;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    @Override
    public double getEvaluations(int j) throws StreamReadException, DatabindException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        gene.mutate();
    }

    @Override
    public void getRandom() throws StreamReadException, DatabindException, IOException {
        gene.randomize();
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

    @JsonIgnore
    @Override
    public double getFitness() throws StreamReadException, DatabindException, IOException {
        return 0;
    }

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        MarketFilterGene newNNConfig =  (MarketFilterGene) gene.crossover(((MarketFilterChromosome2) chromosome).gene);
        MarketFilterChromosome2 eval = new MarketFilterChromosome2(confList, newNNConfig);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new MarketFilterChromosome2(this);
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return gene == null;
    }
    
    @Override
    public String toString() {
        return "" + gene;
    }

}
