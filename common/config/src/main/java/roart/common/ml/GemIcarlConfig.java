package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

@SuppressWarnings("squid:S00117")
public class GemIcarlConfig extends GemConfig {

    private int n_memories;
    
    private double memory_strength;

    private int samples_per_task;
    
    private boolean cuda;    

    @JsonCreator
    public GemIcarlConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("n_layers") int n_layers, 
            @JsonProperty("n_hidden") int n_hiddens, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("n_memories") int n_memories, 
            @JsonProperty("memory_strength") double memory_strength, 
            @JsonProperty("samples_per_task") int samples_per_task,
            @JsonProperty("cuda") boolean cuda) {
        super(MLConstants.ICARL, steps, n_layers, n_hiddens, lr);
        this.n_memories = n_memories;
        this.memory_strength = memory_strength;
        this.samples_per_task = samples_per_task;
        this.cuda = cuda;
    }

    public GemIcarlConfig(String name) {
        super(name);
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

    public int getSamples_per_task() {
        return samples_per_task;
    }

    public void setSamples_per_task(int samples_per_task) {
        this.samples_per_task = samples_per_task;
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
        return super.toString() + " " + n_memories + " " + memory_strength + " " + samples_per_task;
    }
}
