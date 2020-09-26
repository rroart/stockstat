package roart.evolution.iclijconfigmap.jenetics.gene.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.jenetics.Gene;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapMutateCommon;
import roart.iclij.config.IclijConfig;

public final class IclijConfigMapGene implements
Gene<Map<String, Object>, IclijConfigMapGene> {
    private Map<String, Object> map;

    private List<String> confList;
    
    private IclijConfig conf;
    
    public IclijConfigMapGene(IclijConfigMapGene filter) {
        this.map = new HashMap<>(filter.map);
        this.confList = filter.confList;
        this.conf = filter.conf;
    }

    /*
    public IclijConfigMapGene(Map filter) {
        this.map = filter;
    }
    */

    public IclijConfigMapGene(List<String> confList2, IclijConfig config, Map map) {
        this.map = new HashMap<>(map);
        this.confList = confList2;
        this.conf = config;
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

    public IclijConfig getConf() {
        return conf;
    }

    public void setConf(IclijConfig conf) {
        this.conf = conf;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Map<String, Object> getAllele() {
        return map;
    }

    @Override
    public IclijConfigMapGene newInstance() {
        //Map<String, Object> filter = getAllele();
        // int conf2 = new Random().nextInt(this.getConfList().size());
        //new IclijConfigMapMutateCommon().generateConfigNum(new Random(), conf2, this.getConfList(), this.getConf(), this.getMap());
        //Map<String, Object> newFilter = new HashMap<>(map);
        //roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene other = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene(newFilter, filter.categories);
        //other.randomize();
        //return new IclijConfigMapGene(this);
        IclijConfigMapGene other = new IclijConfigMapGene(this);
        for (int i = 0; i < confList.size(); i++) {
            new IclijConfigMapMutateCommon().generateConfigNum(new Random(), i, other.getConfList(), other.getConf(), other.getMap());
        }
        return other;
    }

    @Override
    public IclijConfigMapGene newInstance(Map value) {
        return of(this.confList, this.conf, value);
    }

    static ISeq<IclijConfigMapGene> seq(int count, List<String> confList, IclijConfig config) {
        Map<String, Object> newFilter = new HashMap<>();
        //config.getInccategory(), config.getIncdays(), config.getIncthreshold(), config.getDeccategory(), config.getDecdays(), config.getDecthreshold(), config.getConfidence(), config.getRecordage());
        //roart.evolution.marketfilter.genetics.gene.impl.IclijConfigMapGene other = new roart.evolution.marketfilter.genetics.gene.impl.IclijConfigMapGene(newFilter, config.categories);
        //other.randomize();
        //IclijConfigMapGene other = new IclijConfigMapGene(new HashMap<>());
        IclijConfigMapGene other = new IclijConfigMapGene(confList, config, new HashMap<>());
        for (int i = 0; i < confList.size(); i++) {
            new IclijConfigMapMutateCommon().generateConfigNum(new Random(), i, other.getConfList(), other.getConf(), other.getMap());
        }
        return MSeq.<IclijConfigMapGene>ofLength(count)
                .fill(() -> of(other.confList, other.conf, other.getMap()))
                .toISeq();
    }

    static ISeq<IclijConfigMapGene> seq(int count, List<String> confList, IclijConfig config, Map map) {
        Map<String, Object> newFilter = new HashMap<>();
        //config.getInccategory(), config.getIncdays(), config.getIncthreshold(), config.getDeccategory(), config.getDecdays(), config.getDecthreshold(), config.getConfidence(), config.getRecordage());
        //roart.evolution.marketfilter.genetics.gene.impl.IclijConfigMapGene other = new roart.evolution.marketfilter.genetics.gene.impl.IclijConfigMapGene(newFilter, config.categories);
        //other.randomize();
        //IclijConfigMapGene other = new IclijConfigMapGene(new HashMap<>());
        IclijConfigMapGene other = new IclijConfigMapGene(confList, config, map);
        for (int i = 0; i < confList.size(); i++) {
            new IclijConfigMapMutateCommon().generateConfigNum(new Random(), i, other.getConfList(), other.getConf(), other.getMap());
        }
        return MSeq.<IclijConfigMapGene>ofLength(count)
                .fill(() -> of(other.confList, other.conf, other.getMap()))
                .toISeq();
    }

    public static IclijConfigMapGene of(final IclijConfigMapGene filter) {
        return new IclijConfigMapGene(filter);
    }

    public static IclijConfigMapGene of(List<String> confList, IclijConfig config, final Map filter) {
        return new IclijConfigMapGene(confList, config, filter);
    }

}
