package roart.evolution.marketfilter.jenetics.gene.impl;

import roart.iclij.config.MarketFilter;
import io.jenetics.Gene;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.Mean;
import roart.gene.AbstractGene;
public class MarketFilterGene implements
Gene<MarketFilter, MarketFilterGene>/*,
Mean<MarketFilterGene>*/ {
    private MarketFilter marketfilter;

    public MarketFilterGene(MarketFilter filter) {
        this.marketfilter = filter;
    }

    public MarketFilterGene(int cnt, MarketFilter filter) {
        this.marketfilter = filter;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    /*
    @Override
    public MarketFilterGene mean(MarketFilterGene other) {
        return null; //of(getAllele().mean(other.getAllele()));
    }
    */

    @Override
    public MarketFilter getAllele() {
        return marketfilter;
    }

    @Override
    public MarketFilterGene newInstance() {
        MarketFilter filter = getAllele();
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate mutate = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate(); 
        MarketFilter newFilter = new MarketFilter(filter.getInccategory(), filter.getIncdays(), filter.getIncthreshold(), filter.getDeccategory(), filter.getDecdays(), filter.getDecthreshold(), filter.getConfidence(), filter.getRecordage());
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene other = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene(newFilter);
        other.randomize();
        return new MarketFilterGene(other.getMarketfilter());
    }

    @Override
    public MarketFilterGene newInstance(MarketFilter value) {
        return of(value);
    }

    static ISeq<MarketFilterGene> seq(final int count, MarketFilter filter) {
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate mutate = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate(); 
        MarketFilter newFilter = new MarketFilter(filter.getInccategory(), filter.getIncdays(), filter.getIncthreshold(), filter.getDeccategory(), filter.getDecdays(), filter.getDecthreshold(), filter.getConfidence(), filter.getRecordage());
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene other = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene(newFilter);
        other.randomize();
        return MSeq.<MarketFilterGene>ofLength(count)
                .fill(() -> of(other.getMarketfilter()))
                .toISeq();
    }

    public static MarketFilterGene of(final MarketFilter filter) {
        return new MarketFilterGene(filter);
    }

}
