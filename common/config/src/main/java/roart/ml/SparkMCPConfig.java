package roart.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class SparkMCPConfig extends SparkConfig {
    private Integer maxiter;
    
    private Integer layers;
    
    private int[] nn;

    private Integer prevlayers;
    
    private int[] prevnn;
    
    public SparkMCPConfig(Integer maxiter, Integer layers) {
        super(MLConstants.MCP);
        this.maxiter = maxiter;
        this.layers = layers;
    }

    public SparkMCPConfig() {
        super(MLConstants.MCP);
    }

    public Integer getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(Integer maxiter) {
        this.maxiter = maxiter;
    }

    public Integer getLayers() {
        return layers;
    }

    public void setLayers(Integer layers) {
        this.layers = layers;
    }

    public int[] getNn() {
        return nn;
    }

    public void setNn(int[] nn) {
        this.nn = nn;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        generateMaxiter(rand);
        generateLayers(rand);
        generateNN(rand);
    }

    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(200);
    }

    private void generateNN(Random rand) {
        prevnn = nn;
        nn = new int[layers];
        if (prevnn == null) {
            for(int i = 0; i < layers; i++) {
                nn[i] = ThreadLocalRandom.current().nextInt(3, 50);
            }
        } else {
            
        }
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(3);
        switch (task) {
        case 0:
            generateLayers(rand);
            break;
        case 1:
            mutateNN(rand);
            break;
        case 2:
            generateMaxiter(rand);
            break;
        }
    }

    private void mutateNN(Random rand) {
        if (layers == null || nn == null) {
            int jj = 0;
        }
        int layer = rand.nextInt(layers);
        try {
        nn[layer] = ThreadLocalRandom.current().nextInt(3, 50);
        } catch (Exception e) {
            int jj = 0;
        }
    }

    private void generateLayers(Random rand) {
        prevlayers = layers;
        layers = ThreadLocalRandom.current().nextInt(1, 5);
        if (prevlayers != null && prevlayers.intValue() != layers.intValue()) {
            prevnn = nn;
            nn = new int[layers];
            int min = Math.min(prevlayers, layers);
            if (prevnn != null) {
                for (int i = 0; i < min; i++) {
                    try {
                    nn[i] = prevnn[i];
                    } catch (Exception e) {
                        int jj = 1;
                    }
                }
            }
            for (int i = min; i < layers; i++) {
                nn[i] = ThreadLocalRandom.current().nextInt(3, 50);
            }
        }
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        SparkMCPConfig offspring = new SparkMCPConfig(maxiter, layers);
        offspring.setNn(Arrays.copyOf(nn, nn.length));
        SparkMCPConfig other = (SparkMCPConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.maxiter = other.getMaxiter();
        }
       if (rand.nextBoolean()) {
            offspring.layers = other.getLayers();
        }
        return offspring;

    }

    @Override
    public NNConfig copy() {
        SparkMCPConfig newMCP = new SparkMCPConfig(maxiter, layers);
        if (nn != null) {
            newMCP.setNn(Arrays.copyOf(nn, nn.length));
        }
        return newMCP;
    }
    
    @Override
    public String toString() {
        String array = "";
        if (nn != null) {
            array = Arrays.toString(nn);
        }
        return getName() + " " + layers + " " + array + " " + maxiter;
    }
}
