package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.action.ImproveProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.iclij.verifyprofit.VerifyProfit;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitInputData;

public class ConfigMapChromosome2 extends AbstractChromosome {
    protected ConfigMapGene gene;
    
    public ConfigMapChromosome2(ConfigMapGene gene) {
        this.gene = gene;
    }

    public ConfigMapChromosome2(ConfigMapChromosome2 chromosome) {
        this(chromosome.gene);
    }

    public ConfigMapGene getGene() {
        return gene;
    }

    public void setGene(ConfigMapGene gene) {
        this.gene = gene;
    }

    public Map<String, Object> getMap() {
        return gene.getMap();
    }

    public void setMap(Map<String, Object> map) {
        gene.setMap(map);
    }

    public List<String> getConfList() {
        return gene.getConfList();
    }

    public void setConfList(List<String> confList) {
        gene.setConfList(confList);
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

    /*
    private void configSaves(ComponentData param, Map<String, Object> anUpdateMap) {
        for (Entry<String, Object> entry : anUpdateMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            ConfigItem configItem = new ConfigItem();
            configItem.setAction(param.getAction());
            configItem.setComponent(componentName);
            configItem.setDate(LocalDate.now());
            configItem.setId(key);
            configItem.setMarket(param.getMarket());
            configItem.setRecord(LocalDate.now());
            configItem.setSubcomponent(subcomponent);
            String value = JsonUtil.convert(object);
            configItem.setValue(value);
            try {
                configItem.save();
            } catch (Exception e) {
                log.info(Constants.EXCEPTION, e);
            }
        }
    }
    */

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        ConfigMapGene newNNConfig =  (ConfigMapGene) gene.crossover(((ConfigMapChromosome) chromosome).gene);
        ConfigMapChromosome2 eval = new ConfigMapChromosome2(gene);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        ConfigMapChromosome2 chromosome = new ConfigMapChromosome2(gene);
        chromosome.gene = gene.copy();
        return chromosome;
    }

    @Override
    public boolean isEmpty() {
        return gene.isEmpty();
    }

    @Override
    public String toString() {
        return gene.toString();
    }

    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
