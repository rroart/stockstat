package roart.iclij.config;

public class MLConfig {
    
    // load before evolve or use
    private Boolean load;
    
    // enable evolve or use
    private Boolean enable;

    public Boolean getLoad() {
        return load;
    }

    public void setLoad(Boolean load) {
        this.load = load;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public void merge(MLConfig other) {
        if (other == null) {
            return;
        }
        if (other.load != null) {
            this.load = other.load;
        }
        if (other.enable != null) {
            this.enable = other.enable;
        }
    }
    
}
