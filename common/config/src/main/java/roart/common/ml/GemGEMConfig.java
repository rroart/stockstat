package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

@SuppressWarnings({"squid:S00100", "squid:S00116", "squid:S00117"})
public class GemGEMConfig extends GemConfig {

    private int n_memories;
    
    private double memory_strength;

    private boolean cuda;
    
    @JsonCreator
    public GemGEMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("n_layers") int n_layers, 
            @JsonProperty("n_hiddens") int n_hiddens, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("n_memories") int n_memories, 
            @JsonProperty("memory_strength") double memory_strength,
            @JsonProperty("cuda") boolean cuda) {
        super(MLConstants.GEM, steps, n_layers, n_hiddens, lr);
        this.n_memories = n_memories;
        this.memory_strength = memory_strength;
        this.cuda = cuda;
    }

    public GemGEMConfig(GemGEMConfig config) {
        this(config.steps, config.n_layers, config.n_hiddens, config.lr, config.n_memories, config.memory_strength, config.cuda);
    }

    public GemGEMConfig(String name) {
        super(name);
    }

    public GemGEMConfig() {
        super();
        // JSON
    }

    public int getN_memories() {
        return n_memories;
    }

    public void setN_memories(int n_memories) {
        this.n_memories = n_memories;
    }

    public double getMemory_strength() {
        return memory_strength;
    }

    public void setMemory_strength(double memory_strength) {
        this.memory_strength = memory_strength;
    }

    public boolean isCuda() {
        return cuda;
    }

    public void setCuda(boolean cuda) {
        this.cuda = cuda;
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " " + n_memories + " " + memory_strength;
    }
}
