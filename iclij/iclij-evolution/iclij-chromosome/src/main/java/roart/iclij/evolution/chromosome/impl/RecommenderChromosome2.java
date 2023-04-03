package roart.iclij.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.service.model.ProfitData;

public class RecommenderChromosome2 extends ConfigMapChromosome2 {
    private List<List<String>> listPermBuy = getAllPerms(getBuyList());
    private List<Set<String>> setPermsBuy = makeSet(listPermBuy);
    private List<List<String>> listPermSell = getAllPerms(getSellList());
    private List<Set<String>> setPermsSell = makeSet(listPermSell);
    
    private int listIdx;
    
    public RecommenderChromosome2(ConfigMapGene gene) {
        super(gene);
        this.gene = gene;
    }

    public RecommenderChromosome2(ConfigMapChromosome2 chromosome) {
        this(chromosome.gene);
    }

    private List<Set<String>> makeSet(List<List<String>> listPerm) {
        List<Set<String>> retlist = new ArrayList<>();
        for (List<String> aList : listPerm) {
            retlist.add(new HashSet<>(aList));
        }
        return retlist;
    }

    /*
    public RecommenderChromosome2(ConfigMapGene gene) {
        super(gene);
        Set<String> defaultConfSet = new HashSet<>(defaultConfList);
        Set<String> confSet = new HashSet<>(gene.getConfList());
        Set<String> disableSet = new HashSet<>(defaultConfSet);
        disableSet.removeAll(confSet);
        List<Set<String>> setPerms = getBuy() ? setPermsBuy : setPermsSell;
        listIdx = 0;
        for (Set<String> aSet : setPerms) {
            if (disableSet.size() == aSet.size()) {
                if (disableSet.containsAll(aSet)) {
                    break;
                }
            }
            listIdx++;
        }
        if (listIdx == setPerms.size()) {
            listIdx = 0;
            gene.setConfList(defaultConfList);
        }
    }
    */

    public List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTHISTOGRAMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMACDNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTMACDDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTSIGNALNODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDBUYWEIGHTSIGNALDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSIBUYWEIGHTRSIDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSIBUYWEIGHTSTOCHRSIDELTANODE);
        return buyList;
    }

    public List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTHISTOGRAMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMACDNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTMACDDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTSIGNALNODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXMACDSELLWEIGHTSIGNALDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXRSISELLWEIGHTRSIDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHRSISELLWEIGHTSTOCHRSIDELTANODE);
        return sellList;
    }
    
    public List<String> getConfListThreeBuy() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRBUYWEIGHTATRDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCIBUYWEIGHTCCINODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCIBUYWEIGHTCCIDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHBUYWEIGHTSTOCHNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHBUYWEIGHTSTOCHDELTANODE);
        return list;
    }
    
    public List<String> getConfListThreeSell() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXATRSELLWEIGHTATRDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCISELLWEIGHTCCINODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXCCISELLWEIGHTCCIDELTANODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHSELLWEIGHTSTOCHNODE);
        list.add(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERCOMPLEXSTOCHSELLWEIGHTSTOCHDELTANODE);
        return list;
    }
    
    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        List<List<String>> listPerm = getBuy() ? listPermBuy : listPermSell;
        listIdx = random.nextInt(listPerm.size());
    }
    
    @Override
    public void mutate() {
        int bit = random.nextInt(getBuyList().size());
        //int bit = random.nextInt(bits);
        List<List<String>> listPerm = getBuy() ? listPermBuy : listPermSell;
        listIdx = listIdx ^ (1 << bit);
        if (listIdx >= listPerm.size()) {
            listIdx = 0;
        }
    }
    
    @Override
    public Individual crossover(AbstractChromosome other) {
        RecommenderChromosome2 chromosome = new RecommenderChromosome2(gene);
        int idx = chromosome.listIdx ^ ((RecommenderChromosome2) other).listIdx;
        chromosome.listIdx = idx;
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
        RecommenderChromosome2 chromosome = new RecommenderChromosome2(gene);
        chromosome.gene = gene.copy();
        chromosome.listIdx = listIdx;
        return chromosome;
    }
    
    @Override
    public String toString() {
        Set<String> conf = new HashSet<>(gene.getConfList());
        List<List<String>> listPerm = getBuy() ? listPermBuy : listPermSell;
        Set<String> disable = new HashSet<>(listPerm.get(listIdx));
        conf.removeAll(disable);
        return conf.toString();
    }
    
    // duplicated
    public static List<List<String>> getAllPerms(List<String> list) {
        List<List<String>> listPerm = new ArrayList<>();
        int size = list.size();
        int bitsize = (1 << size) - 1;
        for (int i = 0; i < bitsize; i++) {
            List<String> aList = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if ((i & (1 << j)) != 0) {
                    aList.add(list.get(j));
                }
            }
            listPerm.add(aList);
        }
        return listPerm;
    }

    public boolean getBuy() {
        return true;
    }
}
