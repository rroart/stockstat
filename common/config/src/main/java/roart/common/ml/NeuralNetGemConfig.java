package roart.common.ml;

public class NeuralNetGemConfig {

    private GemEWCConfig gemEWCConfig;
    
    private GemGEMConfig gemGEMConfig;
    
    private GemIcarlConfig gemIcarlConfig;
    
    private GemIConfig gemIConfig;
    
    private GemMMConfig gemMMConfig;
    
    private GemSConfig gemSConfig;

    public GemEWCConfig getGemEWCConfig() {
        return gemEWCConfig;
    }

    public void setGemEWCConfig(GemEWCConfig gemEWCConfig) {
        this.gemEWCConfig = gemEWCConfig;
    }

    public GemGEMConfig getGemGEMConfig() {
        return gemGEMConfig;
    }

    public void setGemGEMConfig(GemGEMConfig gemGEMConfig) {
        this.gemGEMConfig = gemGEMConfig;
    }

    public GemIcarlConfig getGemIcarlConfig() {
        return gemIcarlConfig;
    }

    public void setGemIcarlConfig(GemIcarlConfig gemIcarlConfig) {
        this.gemIcarlConfig = gemIcarlConfig;
    }

    public GemIConfig getGemIConfig() {
        return gemIConfig;
    }

    public void setGemIConfig(GemIConfig gemIConfig) {
        this.gemIConfig = gemIConfig;
    }

    public GemMMConfig getGemMMConfig() {
        return gemMMConfig;
    }

    public void setGemMMConfig(GemMMConfig gemMMConfig) {
        this.gemMMConfig = gemMMConfig;
    }

    public GemSConfig getGemSConfig() {
        return gemSConfig;
    }

    public void setGemSConfig(GemSConfig gemSConfig) {
        this.gemSConfig = gemSConfig;
    }

    public NeuralNetGemConfig(GemEWCConfig gemEWCConfig, GemGEMConfig gemGEMConfig, GemIcarlConfig gemIcarlConfig,
            GemIConfig gemIConfig, GemMMConfig gemMMConfig,
            GemSConfig gemSConfig) {
        super();
        this.gemEWCConfig = gemEWCConfig;
        this.gemGEMConfig = gemGEMConfig;
        this.gemIcarlConfig = gemIcarlConfig;
        this.gemIConfig = gemIConfig;
        this.gemMMConfig = gemMMConfig;
        this.gemSConfig = gemSConfig;
    }
    
    
}
