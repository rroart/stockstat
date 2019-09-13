package roart.common.ml;

import roart.common.config.MLConstants;

public class GemGEMConfig extends GemConfig {

    private int memories;
    
    private double memorystrength;

    public GemGEMConfig(int steps, int layers, int hidden, double lr, int memories, double memorystrength) {
        super(MLConstants.GEM, steps, layers, hidden, lr);
        this.memories = memories;
        this.memorystrength = memorystrength;
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

}
