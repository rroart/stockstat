package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowFeedConfig;
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
    @Type(value = TensorflowEstimatorConfigGene.class, name = "TensorflowEstimatorConfigGene"),
    @Type(value = TensorflowFeedConfigGene.class, name = "TensorflowFeedConfigGene"),
    @Type(value = TensorflowPreFeedConfigGene.class, name = "TensorflowPreFeedConfigGene") })  
public abstract class TensorflowConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 12;
    
    public TensorflowConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        myconfig.setSteps(generateSteps());
        if (random.nextBoolean()) {
            myconfig.setLr(generateLr());
        }
        myconfig.setInputdropout(generateDropout());
        myconfig.setDropout(generateDropout());
        myconfig.setNormalize(generateBoolean());
        myconfig.setBatchnormalize(generateBoolean());
        myconfig.setRegularize(generateBoolean());
        myconfig.setBatchsize(generateBatchsize());
        myconfig.setLoss(generateLoss());
        myconfig.setOptimizer(generateOptimizer());
        myconfig.setActivation(generateActivation());
        myconfig.setLastactivation(generateLastactivation());
    }

    public void mutate(int task) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setSteps(generateSteps());
            break;
        case 1:
            myconfig.setLr(generateLr());
            break;
        case 2:
            myconfig.setInputdropout(generateDropout());
            break;
        case 3:
            myconfig.setDropout(generateDropout());
            break;
        case 4:
            myconfig.setNormalize(generateBoolean());
            break;
        case 5:
            myconfig.setBatchnormalize(generateBoolean());
            break;
        case 6:
            myconfig.setRegularize(generateBoolean());
            break;
        case 7:
            myconfig.setBatchsize(generateBatchsize());
            break;
        case 8:
            myconfig.setLoss(generateLoss());
            break;
        case 9:
            myconfig.setOptimizer(generateOptimizer());
            break;
        case 10:
            myconfig.setActivation(generateActivation());
            break;
        case 11:
            myconfig.setLastactivation(generateLastactivation());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    public void crossover(TensorflowConfigGene other) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        TensorflowConfig otherconfig = (TensorflowConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSteps(otherconfig.getSteps());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
        if (random.nextBoolean()) {
            myconfig.setInputdropout(otherconfig.getInputdropout());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout(otherconfig.getDropout());
        }
        if (random.nextBoolean()) {
            myconfig.setNormalize(otherconfig.isNormalize());
        }
        if (random.nextBoolean()) {
            myconfig.setBatchnormalize(otherconfig.isBatchnormalize());
        }
        if (random.nextBoolean()) {
            myconfig.setRegularize(otherconfig.isRegularize());
        }        
        if (random.nextBoolean()) {
            myconfig.setBatchsize(otherconfig.getBatchsize());
        }
        if (random.nextBoolean()) {
            myconfig.setLoss(otherconfig.getLoss());
        }
        if (random.nextBoolean()) {
            myconfig.setOptimizer(otherconfig.getOptimizer());
        }
        if (random.nextBoolean()) {
            myconfig.setActivation(otherconfig.getActivation());
        }
        if (random.nextBoolean()) {
            myconfig.setLastactivation(otherconfig.getLastactivation());
        }
    }
    
    protected String generateLoss() {
        String[] losses = { "binary_crossentropy", "categorical_crossentropy", "sparse_categorical_crossentropy", "poisson", "ctc", "kl_divergence" };
        //"l1", "mse", "cross_entropy", "ctc", "nl", "poissonnl", "gaussiannl", "kl", "bce", "bcewithlogits", "marginrank", "hingeembedding", "multilabelmargin", "multilabelsoftmargin", "cosineembedding", "multimargin", "tripletmargin", "tripletmarginwithdistance" };
        //String[] losses = { "l1", "mse", "cross_entropy", "ctc", "nl", "poissonnl", "gaussiannl", "kl", "bce", "bcewithlogits", "marginrank", "hingeembedding", "multilabelmargin", "multilabelsoftmargin", "cosineembedding", "multimargin", "tripletmargin",  "tripletmarginwithdistance" };
        if (predictor) {
            //losses = new String[] { "mean_squared_error", "mean_absolute_error", "mean_absolute_percentage_error", "mean_squared_logarithmic_error", "cosine_similarity", "huber", "log_cosh", "tversky", "dice" };
        }
        return losses[random.nextInt(losses.length)];
    }
    
    protected String generateOptimizer() {
        String[] optimizers = { "adadelta", "adagrad", "adam", "adamw", "adamax", "ftrl", "nadam", "rmsprop", "sgd", "lion", "lamb", "muon", "loss_scale_optimizer", "adafactor" };
        return optimizers[random.nextInt(optimizers.length)];
    }
    
    protected String generateActivation() {
        String[] activations = { "celu", "elu", "exponential", "gelu", "glu", "hard_shrink", "hard_sigmoid", "hard_silu", "hard_tanh", "leaky_relu", "linear", "log_sigmoid", "log_softmax", "mish", "relu", "relu6", "selu", "sigmoid", "silu", "softmax", "soft_shrink", "softplus", "softsign", "sparse_plus", "sparsemax", "squareplus", "tanh", "tanh_shrink", "threshold" };
        return activations[random.nextInt(activations.length)];
    }
    
    protected String generateLastactivation() {
        String[] activations = { "celu", "elu", "exponential", "gelu", "glu", "hard_shrink", "hard_sigmoid", "hard_silu", "hard_tanh", "leaky_relu", "linear", "log_sigmoid", "log_softmax", "mish", "relu", "relu6", "selu", "sigmoid", "silu", "softmax", "soft_shrink", "softplus", "softsign", "sparse_plus", "sparsemax", "squareplus", "tanh", "tanh_shrink", "threshold" };
        if (predictor) {
            activations = new String[] { "linear" };
        }
        return activations[random.nextInt(activations.length)];
    }
    
}

