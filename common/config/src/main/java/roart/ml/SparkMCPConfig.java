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

    private Double tol;
    
    public SparkMCPConfig(Integer maxiter, Integer layers, Double tol) {
        super(MLConstants.MCP);
        this.maxiter = maxiter;
        this.layers = layers;
	this.tol = tol;
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

    public Double getTol() {
        return tol;
    }

    public void setTol(Double tol) {
        this.tol = tol;
    }

    @Override
    public void randomize() {
        //validate();
        Random rand = new Random();
        generateMaxiter(rand);
        generateLayers(rand);
	tol = generateTol();
        //generateNN(rand);
        validate();
    }

    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(MAX_ITER);
    }

    private void generateNN(Random rand) {
        prevnn = nn;
        nn = new int[layers];
        if (prevnn == null) {
            for(int i = 0; i < layers; i++) {
                nn[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            }
        } else {

        }
    }

    @Override
    public void mutate() {
        validate();
        Random rand = new Random();
        int task = rand.nextInt(4);
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
	case 3:
	    tol = generateTol();
        }
        validate();
    }

    private void mutateNN(Random rand) {
        if (layers == null || nn == null) {
            int jj = 0;
        }
        int layer = rand.nextInt(layers);
        try {
            nn[layer] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
        } catch (Exception e) {
            int jj = 0;
        }
    }

    private void generateLayers(Random rand) {
        prevlayers = layers;
        layers = ThreadLocalRandom.current().nextInt(1, MAX_LAYERS + 1);
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
                nn[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            } 
        } else {
            if (nn == null) {
                nn = new int[layers];
            } else {
                int jj = 0;
            }
            for(int i = 0; i < layers; i++) {
                nn[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            }
        }
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        validate();
        SparkMCPConfig offspring = new SparkMCPConfig(maxiter, layers, tol);
        offspring.setNn(Arrays.copyOf(nn, nn.length));
        SparkMCPConfig other = (SparkMCPConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.maxiter = other.getMaxiter();
        }
        if (rand.nextBoolean()) {
            offspring.layers = other.getLayers();
            offspring.nn = Arrays.copyOf(other.nn, other.nn.length);
        }
        offspring.validate();
        return offspring;
    }

    @Override
    public NNConfig copy() {
        SparkMCPConfig newMCP = new SparkMCPConfig(maxiter, layers, tol);
        if (nn != null) {
            newMCP.setNn(Arrays.copyOf(nn, nn.length));
        }
        return newMCP;
    }

    @Override
    public boolean empty() {
        return maxiter == null;
    }
    
    @Override
    public String toString() {
        String array = "";
        if (nn != null) {
            array = Arrays.toString(nn);
        }
        return getName() + " " + layers + " " + array + " " + maxiter + " " + tol;
    }

    private void validate() {
        if (maxiter == null || maxiter == 0) {
            int jj = 0;
        }
        if (layers == null || layers == 0) {
            int jj = 0;
        }
        if (layers != null && nn != null) {
            if (layers != nn.length) {
                int jj = 0;
            }
        }
        if (nn == null) {
            int jj = 0;
        } else {
            for (int i = 0; i < layers; i++) {
                if (nn[i] == 0) {
                    int jj = 0;
                }
            }
        }
    }
}
