package roart.evolution.iclijconfigmap.genetics.gene.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Random;

import roart.common.util.MathUtil;
import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapCrossoverCommon;
import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapMutateCommon;
import roart.gene.AbstractGene;
import roart.iclij.config.IclijConfig;

public class IclijConfigMapGene extends AbstractGene {
    private Map<String, Object> map = new HashMap<>();

    protected List<String> confList;

    private IclijConfig conf;
    
    public IclijConfigMapGene(List<String> confList, IclijConfig conf) {
        this.confList = confList;
        this.conf = conf;
    }

    public IclijConfigMapGene() {
        // TODO Auto-generated constructor stub
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    @Override
    public void randomize() {
        for (int conf = 0; conf < confList.size(); conf++) {
            new IclijConfigMapMutateCommon().generateConfigNum(random, conf, confList, this.conf, map);
        }
        /*
        if (!validate()) {
            fixValidation();
        }
        */
    }

    @Override
    public void mutate() {
        int conf = random.nextInt(confList.size());
        new IclijConfigMapMutateCommon().generateConfigNum(random, conf, confList, this.conf, map);
        /*
        if (!validate()) {
            fixValidation();
        }
        */
    }

    @Override
    public AbstractGene crossover(AbstractGene other) {
        IclijConfigMapGene newGene = new IclijConfigMapGene();
        newGene.conf = this.conf;
        newGene.confList = this.confList;
        /*
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (random.nextBoolean()) {
                newGene.map.put(confName, this.map.get(confName));
            } else {
                newGene.map.put(confName, ((IclijConfigMapGene) other).map.get(confName));
            }
        }
        */
        Map<String, Object> neww = new IclijConfigMapCrossoverCommon().crossover(getConfList(), map, ((IclijConfigMapGene) other).map);
        newGene.map = neww;
        /*
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        */
        return newGene;
    }
    
    public IclijConfigMapGene copy() {
        IclijConfigMapGene gene = new IclijConfigMapGene();
        gene.map = new HashMap<>(this.map);
        gene.conf = this.conf;
        gene.confList = this.confList;
        return gene;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return confList == null || confList.isEmpty() || map == null || map.isEmpty();
    }
    
    @Override
    public String toString() {
        return map.toString();
        /*
        String str = "";
        for (Entry<String, Object> entry : map.entrySet()) {
            str = str + "       " + entry.getKey() + " " + entry.getValue() + "\n";
        }
        return str;
        */
    }

}
