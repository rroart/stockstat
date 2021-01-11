package roart.gene.ml.impl;

import roart.common.ml.NeuralNetConfig;
import roart.common.ml.SparkConfig;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = SparkLORConfigGene.class, name = "SparkLORConfigGene"),
    @Type(value = SparkLSVCConfigGene.class, name = "SparkLSVCConfigGene"),
    @Type(value = SparkMLPCConfigGene.class, name = "SparkMLPCConfigGene"),
    @Type(value = SparkOVRConfigGene.class, name = "SparkOVRConfigGene") })  
public abstract class SparkConfigGene extends NeuralNetConfigGene {

    protected static final int RANDOMS = 2;
    
    public SparkConfigGene(NeuralNetConfig config) {
        super(config);
    }

    public SparkConfigGene() {
        // JSON
    }

    @Override
    public void randomize() {
        SparkConfig myconfig = (SparkConfig) getConfig();
        myconfig.setMaxiter(generateSteps());
        myconfig.setTol(generateTol());
    }
    
    public void mutate(int task) {
        SparkConfig myconfig = (SparkConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setMaxiter(generateSteps());
            break;
        case 1:
            myconfig.setTol(generateTol());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    public void crossover(SparkConfigGene other) {
        //super.crossover(other);
        SparkConfig myconfig = (SparkConfig) getConfig();
        SparkConfig otherconfig = (SparkConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setMaxiter(otherconfig.getMaxiter());
        }
        if (random.nextBoolean()) {
            myconfig.setTol(otherconfig.getTol());
        }
    }
    protected boolean generateFitintercept() {
        return random.nextBoolean();
    }
    
}
