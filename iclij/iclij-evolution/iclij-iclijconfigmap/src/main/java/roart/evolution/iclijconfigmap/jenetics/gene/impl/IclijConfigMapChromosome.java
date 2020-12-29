package roart.evolution.iclijconfigmap.jenetics.gene.impl;

import java.util.List;
import java.util.Map;

import io.jenetics.AbstractChromosome;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import roart.iclij.config.IclijConfig;

public final class IclijConfigMapChromosome extends AbstractChromosome<IclijConfigMapGene> {

    protected IclijConfigMapChromosome(ISeq<? extends IclijConfigMapGene> genes) {
        super(genes);
    }

    /*
    public IclijConfigMapChromosome(Map<String, Object> map) {
        super(map);
    }
    */

    public IclijConfigMapChromosome(List<String> confList, IclijConfig config) {
        super(IclijConfigMapGene.seq(1, confList, config));
    }

    @Override
    public Chromosome<IclijConfigMapGene> newInstance(ISeq<IclijConfigMapGene> genes) {
        IclijConfigMapGene mygene = genes.asList().get(0);
        ISeq<IclijConfigMapGene> iseq = mygene.seq(1, mygene.getConfList(), mygene.getConf(), mygene.getMap());
        return new IclijConfigMapChromosome(iseq);
    }

    @Override
    public Chromosome<IclijConfigMapGene> newInstance() {
        int i = length();
        IclijConfigMapGene gene = getGene();
        gene = new IclijConfigMapGene(gene);
        //return new IclijConfigMapChromosome(getGene().getAllele());
        return new IclijConfigMapChromosome(gene.getConfList(), gene.getConf());
    }

}
