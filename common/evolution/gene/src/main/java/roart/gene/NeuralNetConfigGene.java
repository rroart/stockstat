package roart.gene;

import roart.common.ml.NeuralNetConfig;
import roart.common.util.RandomUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NeuralNetConfigGene extends AbstractGene {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfig config;
    
    public NeuralNetConfig getConfig() {
        return config;
    }

    public void setConfig(NeuralNetConfig config) {
        this.config = config;
    }

    public NeuralNetConfigGene(NeuralNetConfig config) {
        super();
        this.config = config;
    }

    protected int generateSteps() {
        return RandomUtil.random(random, 10, 10, 100);
    }

    protected double generateLr() {
        return RandomUtil.generatePow(random, 0.1, 1, 8);
    }

    protected int generateLayers() {
        return RandomUtil.random(random, 1, 5);
    }

    protected int generateHidden() {
        return RandomUtil.random(random, 10, 10, 10);
    }
    
    protected int generateSlide() {
        return RandomUtil.random(random, 1, 5);
    }

    protected int generateStride() {
        return RandomUtil.random(random, 1, 5);
    }

    protected int generateKernelsize() {
        return RandomUtil.random(random, 1, 5);
    }

    public double generateTol() {
        return RandomUtil.generatePow(random, 0.1, 1, 8);
    }
    
    public double generateDropout() {
        return RandomUtil.random(random, 0.5, 0.1, 3);
    }
    
    public double generateDropoutIn() {
        return RandomUtil.random(random, 0.8, 0.1, 2);
    }
}
