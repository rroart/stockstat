package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import roart.common.config.ConfigConstants;

public class MLConfigs {

    private EvolveMLSparkConfig spark;
    
    private EvolveMLTensorflowConfig tensorflow;
    
    private EvolveMLPytorchConfig pytorch;
    
    private EvolveMLGemConfig gem;
    
    public MLConfigs() {
        super();
        this.spark = new EvolveMLSparkConfig();
        this.tensorflow = new EvolveMLTensorflowConfig();
        this.pytorch = new EvolveMLPytorchConfig();
        this.gem = new EvolveMLGemConfig();
    }

    public EvolveMLSparkConfig getSpark() {
        return spark;
    }

    public void setSpark(EvolveMLSparkConfig spark) {
        this.spark = spark;
    }

    public EvolveMLTensorflowConfig getTensorflow() {
        return tensorflow;
    }

    public void setTensorflow(EvolveMLTensorflowConfig tensorflow) {
        this.tensorflow = tensorflow;
    }

    public EvolveMLPytorchConfig getPytorch() {
        return pytorch;
    }

    public void setPytorch(EvolveMLPytorchConfig pytorch) {
        this.pytorch = pytorch;
    }

    public EvolveMLGemConfig getGem() {
        return gem;
    }

    public void setGem(EvolveMLGemConfig gem) {
        this.gem = gem;
    }

    /**
     * 
     * Merge in other by override if not null
     * 
     * @param mlConfigs
     * @return
     */
    
    public void merge(MLConfigs mlConfigs) {
        if (mlConfigs == null) {
            return;
        }
        spark.merge(mlConfigs.spark);
        tensorflow.merge(mlConfigs.tensorflow);
        pytorch.merge(mlConfigs.pytorch);
        gem.merge(mlConfigs.gem);
        
    }

    @JsonIgnore
    public Map<String, EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.putAll(spark.getAll());
        map.putAll(tensorflow.getAll());
        map.putAll(pytorch.getAll());
        map.putAll(gem.getAll());
        return map;
    }
}
