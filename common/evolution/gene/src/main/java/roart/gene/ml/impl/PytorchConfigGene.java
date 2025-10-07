package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchFeedConfig;
import roart.common.util.RandomUtil;
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
    @Type(value = PytorchFeedConfigGene.class, name = "PytorchFeedConfigGene"),
    @Type(value = PytorchPreFeedConfigGene.class, name = "PytorchPreFeedConfigGene") })  
public abstract class PytorchConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 12;
    
    public PytorchConfigGene(PytorchConfig config) {
        super(config);
    }

    public PytorchConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        PytorchConfig myconfig = (PytorchConfig) getConfig();
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
        PytorchConfig myconfig = (PytorchConfig) getConfig();
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

    public void crossover(PytorchConfigGene other) {
        PytorchConfig myconfig = (PytorchConfig) getConfig();
        PytorchConfig otherconfig = (PytorchConfig) other.getConfig();
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
    
    protected int getMemories() {
        return RandomUtil.random(random, 10, 10, 100);
    }

    protected double getMemorystrength() {
        return RandomUtil.random(random, 0.5, 0.5, 5);
    }
    
    protected String generateLoss() {
        //String[] losses = { "mse", "nllloss", "crossentropyloss", "l1loss", "smoothl1loss", "poissonloss" };
        String[] losses = { "l1", "mse", "cross_entropy", "ctc", "nl", "poissonnl", "gaussiannl", "kl", "bce", "bcewithlogits", "marginrank", "hingeembedding", "multilabelmargin", "multilabelsoftmargin", "cosineembedding", "multimargin", "tripletmargin",  "tripletmarginwithdistance" };
                if (predictor) {
            losses = new String[] { "mse", "l1loss", "smoothl1loss", "poissonloss" };
        }
        return losses[random.nextInt(losses.length)];
    }
    
    protected String generateOptimizer() {
        // TODO add more optimizers
        //String[] optimizers = { "adadelta", "adagrad", "adam", "adamw", "adamax", "ftrl", "nadam", "rmsprop", "sgd", "lion", "lamb", "muon", "loss_scale_optimizer", "adafactor", "sparseadam", "asgd", "lbfgs", "radam", "rprop", "lrscheduler", "lambdalr", "multiplicativelr", "steplr", "exponentiallr", "cosineannealinglr", "cycliclr", "onecyclelr", "reducelronplateau" };
        String[] optimizers = { "adadelta", "adafactor", "adagrad", "adam", "adamw", "sparseadam", "adamax", "asgd", "lbfgs", "nadam", "radam", "rmsprop", "rprop", "sgd", "lr_scheduler", "lambda_lr", "multiplicatative_lr", "step_lr", "multistep_lr", "constant_lr", "linear_lr", "exponential_lr", "polynomial_lr", "cosine_annealing_lr", "chained_scheduler", "sequential_lr", "reduce_lr_on_plateau", "cyclic_lr", "one_cycle_lr", "cosine_annealing_warm_restarts", "averaged_model", "swa_lr" };
        return optimizers[random.nextInt(optimizers.length)];
    }

    protected String generateActivation() {
        //String[] activations = { "relu", "leakyrelu", "tanh", "sigmoid", "softmax", "softplus", "softsign", "elu", "selu", "gelu" };
        String[] activations = { "elu", "hard_shrink", "hard_sigmoid", "hard_tanh", "hard_swish", "leakyrelu", "log_sigmoid", "multihead_attention", "prelu", "relu", "relu6", "rrelu", "selu", "celu", "gelu", "sigmoid", "silu", "mish", "soft_plus", "soft_shrink", "soft_sign", "tanh", "tanh_shrink", "threshold", "glu", "soft_min", "soft_max", "soft_max_2d", "log_soft_max", "adaptive_log_soft_max_with_loss" };
        return activations[random.nextInt(activations.length)];
    }
    
    protected String generateLastactivation() {
        String[] activations = { "relu", "leakyrelu", "tanh", "sigmoid", "softmax", "softplus", "softsign", "elu", "selu", "gelu", "none" };
        if (predictor) {
            activations = new String[] { "relu", "leakyrelu", "tanh", "sigmoid", "softmax", "softplus", "softsign", "elu", "selu", "gelu" };
        }
        return activations[random.nextInt(activations.length)];
    }
}

