package roart.common.ml;

import roart.common.config.MLConstants;

public class GemEWCConfig extends GemConfig {

    private int memories;
    
    private double memorystrength;

    public GemEWCConfig(int steps, int layers, int hidden, double lr, int memories, double memorystrength) {
        super(MLConstants.EWC, steps, layers, hidden, lr);
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

    @Override
    public String toString() {
        return super.toString() + " " + memories + " " + memorystrength;
    }
}
