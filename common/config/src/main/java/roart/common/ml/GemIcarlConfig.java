package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class GemIcarlConfig extends GemConfig {

    private int memories;
    
    private double memorystrength;

    private int samplespertask;
    
    @JsonCreator
    public GemIcarlConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("memories") int memories, 
            @JsonProperty("memorystrength") double memorystrength, 
            @JsonProperty("samplespertask") int samplespertask) {
        super(MLConstants.GEM, steps, layers, hidden, lr);
        this.memories = memories;
        this.memorystrength = memorystrength;
        this.samplespertask = samplespertask;
    }

    public GemIcarlConfig(String name) {
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

    public int getSamplespertask() {
        return samplespertask;
    }

    public void setSamplespertask(int samplespertask) {
        this.samplespertask = samplespertask;
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " " + memories + " " + memorystrength + " " + samplespertask;
    }
}
