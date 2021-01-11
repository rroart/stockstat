package roart.common.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

public class SparkMLPCConfig extends SparkConfig {

    private Integer layers;

    private int hidden;

    @JsonCreator
    public SparkMLPCConfig(
            @JsonProperty("maxiter") Integer maxiter, 
            @JsonProperty("tol") double tol, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) {
        super(MLConstants.MLPC, maxiter, tol);
        this.layers = layers;
        this.hidden = hidden;
    }

    public SparkMLPCConfig(SparkMLPCConfig config) {
        this(config.getMaxiter(), config.getTol(), config.getLayers(), config.getHidden());
    }

    public SparkMLPCConfig(String name) {
        super(name);
    }

    public SparkMLPCConfig() {
        super();
        // JSON
    }

    public Integer getLayers() {
        return layers;
    }

    public void setLayers(Integer layers) {
        this.layers = layers;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }

}
