package roart.gene.ml.impl;

import roart.common.ml.GemIConfig;
import roart.gene.AbstractGene;

public class GemIConfigGene extends GemConfigGene {

    public GemIConfigGene(GemIConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        generateFinetune();
        generateCuda();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS + 2);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
            case 0:
                generateFinetune();
                break;
            case 1:
                generateCuda();
                break;
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        GemIConfigGene offspring = new GemIConfigGene((GemIConfig) getConfig());
        GemIConfigGene other = (GemIConfigGene) otherNN;
        GemIConfig offspringconfig = (GemIConfig) offspring.getConfig();
        GemIConfig otherconfig = (GemIConfig) other.getConfig();

        offspring.crossover((GemConfigGene) otherNN);
        if (random.nextBoolean()) {
            offspringconfig.setFinetune(otherconfig.isFinetune());
        }
        if (random.nextBoolean()) {
            offspringconfig.setCuda(otherconfig.isCuda());
        }
        return offspring;
    }

    private void generateFinetune() {
        GemIConfig myconfig = (GemIConfig) getConfig();
        myconfig.setFinetune(random.nextBoolean());
    }

    private void generateCuda() {
        GemIConfig myconfig = (GemIConfig) getConfig();
        myconfig.setCuda(random.nextBoolean());
    }

}
