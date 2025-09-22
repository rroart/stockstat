package roart.gene;

import roart.common.ml.NeuralNetConfig;
import roart.common.util.RandomUtil;
import roart.gene.ml.impl.GemConfigGene;
import roart.gene.ml.impl.PytorchConfigGene;
import roart.gene.ml.impl.PytorchLSTMConfigGene;
import roart.gene.ml.impl.SparkConfigGene;
import roart.gene.ml.impl.TensorflowConfigGene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = GemConfigGene.class, name = "GemConfigGene"),
    @Type(value = PytorchConfigGene.class, name = "PytorchConfigGene"),
    @Type(value = SparkConfigGene.class, name = "SparkConfigGene"),
    @Type(value = TensorflowConfigGene.class, name = "TensorflowConfigGene") })  
public abstract class NeuralNetConfigGene extends AbstractGene {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfig config;
    
    protected boolean predictor = false;
    
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

    public NeuralNetConfigGene() {        
        // JSON
    }
    
    public abstract NeuralNetConfigGene copy();

    protected boolean generateBoolean() {
        return random.nextBoolean();
    }
    
    protected int generateSteps() {
        return RandomUtil.random(random, 100, 100, 10);
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

    protected int generateMaxpool() {
        return RandomUtil.random(random, 1, 5);
    }

    public double generateTol() {
        return RandomUtil.generatePow(random, 0.1, 1, 8);
    }
    
    public double generateDropout() {
        return RandomUtil.random(random, 0.2, 0.1, 4);
    }
    
    public double generateDropoutIn() {
        return RandomUtil.random(random, 0.0, 0.1, 3);
    }
    
    protected int generateBatchsize() {
        return (int) RandomUtil.generatePow(random, 2, 16, 8192);
    }

    public String toString() {
        return config.toString();
    }

}
