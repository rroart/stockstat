package roart.evolution.marketfilter.chromosome.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        gene.mutate();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        gene.randomize();
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
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        MarketFilterGene newNNConfig =  (MarketFilterGene) gene.crossover(((MarketFilterChromosome2) chromosome).gene);
        MarketFilterChromosome2 eval = new MarketFilterChromosome2(confList, gene);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new MarketFilterChromosome2(this);
    }

    @Override
    public boolean isEmpty() {
        return gene == null;
    }
    
    @Override
    public String toString() {
        return "" + gene;
    }

}
