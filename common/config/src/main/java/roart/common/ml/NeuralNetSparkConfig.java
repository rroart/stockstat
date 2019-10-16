package roart.common.ml;

public class NeuralNetSparkConfig {

    private SparkLORConfig sparkLORConfig;
    private SparkMLPCConfig sparkMLPCConfig;
    private SparkOVRConfig sparkOVRConfig;
    private SparkLSVCConfig sparkLSVCConfig;

    public NeuralNetSparkConfig() {
        super();
    }

    public NeuralNetSparkConfig(SparkLORConfig sparkLRConfig, SparkMLPCConfig sparkMLPCConfig,
            SparkOVRConfig sparkOVRConfig, SparkLSVCConfig sparkLSVCConfig) {
        super();
        this.sparkLORConfig = sparkLRConfig;
        this.sparkMLPCConfig = sparkMLPCConfig;
        this.sparkOVRConfig = sparkOVRConfig;
        this.sparkLSVCConfig = sparkLSVCConfig;
    }

    public SparkLORConfig getSparkLORConfig() {
        return sparkLORConfig;
    }

    public void setSparkLORConfig(SparkLORConfig sparkLORConfig) {
        this.sparkLORConfig = sparkLORConfig;
    }

    public SparkMLPCConfig getSparkMLPCConfig() {
        return sparkMLPCConfig;
    }

    public void setSparkMLPCConfig(SparkMLPCConfig sparkMLPCConfig) {
        this.sparkMLPCConfig = sparkMLPCConfig;
    }

    public SparkOVRConfig getSparkOVRConfig() {
        return sparkOVRConfig;
    }

    public void setSparkOVRConfig(SparkOVRConfig sparkOVRConfig) {
        this.sparkOVRConfig = sparkOVRConfig;
    }

    public SparkLSVCConfig getSparkLSVCConfig() {
        return sparkLSVCConfig;
    }

    public void setSparkLSVCConfig(SparkLSVCConfig sparkLSVCConfig) {
        this.sparkLSVCConfig = sparkLSVCConfig;
    }

}
