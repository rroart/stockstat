package roart.iclij.config;

public class EvolveMLConfig extends MLConfig {

    // save after evolve
    private Boolean save;    

    private Boolean evolve;
    
    public Boolean getSave() {
        return save;
    }

    public void setSave(Boolean save) {
        this.save = save;
    }

    public Boolean getEvolve() {
        return evolve;
    }

    public void setEvolve(Boolean evolve) {
        this.evolve = evolve;
    }

    /**
     * 
     * Merge in other by override if not null
     * 
     * @param other ML config
     */
    
    public void merge(EvolveMLConfig other) {
        super.merge(other);
        if (other == null) {
            return;
        }
        if (other.evolve != null) {
            this.evolve = other.evolve;
        }
        if (other.save != null) {
            this.save = other.save;
        }
    }
    
}
