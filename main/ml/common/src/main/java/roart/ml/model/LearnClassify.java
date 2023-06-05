package roart.ml.model;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.DoubleArraySerializer;

public class LearnClassify {
    private String id;
    
    private Object array;
    
    private Double classification;

    // for jackson
    public LearnClassify() {
        super();
    }

    public LearnClassify(String id, Object array, Double classification) {
        super();
        this.id = id;
        this.array = array;
        this.classification = classification;
    }

    public LearnClassify(String id, Object array, Integer classification) {
        super();
        this.id = id;
        this.array = array;
        this.classification = classification != null ? classification.doubleValue() : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getArray() {
        return array;
    }

    public void setArray(double[] array) {
        this.array = array;
    }

    public Double getClassification() {
        return classification;
    }

    public void setClassification(Double classification) {
        this.classification = classification;
    }
    
}
