package roart.iclij.evolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jenetics.Phenotype;
import roart.common.constants.Constants;
import roart.common.model.MLMetricsDTO;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.Market;
import roart.iclij.config.MarketFilter;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public abstract class EvolveJ extends Evolve {

    protected void print(String title, List<Phenotype<roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene, Double>> population) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            for (Phenotype<roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene, Double> pt : population) {
                MarketFilter filter = pt.getGenotype().getChromosome().getGene().getAllele();
                String individual = pt.getFitness() + " #" + pt.hashCode() + " " + filter.toString();
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
}
