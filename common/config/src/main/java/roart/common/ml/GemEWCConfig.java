package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class GemEWCConfig extends GemConfig {

    private int memories;
    
    private double memorystrength;

    @JsonCreator
    public GemEWCConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("memories") int memories, 
            @JsonProperty("memorystrength") double memorystrength) {
        super(MLConstants.EWC, steps, layers, hidden, lr);
        this.memories = memories;
        this.memorystrength = memorystrength;
    }

    public GemEWCConfig(String name) {
        super(name);
    }

    public int getMemories() {
        return memories;
    }

    public void setMemories(int memories) {
        this.memories = memories;
    }

    public double getMemorystrength() {
        return memorystrength;
    }

    public void setMemorystrength(double memorystrength) {
        this.memorystrength = memorystrength;
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " " + memories + " " + memorystrength;
    }
}
