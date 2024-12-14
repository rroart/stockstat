package roart.common.pipeline.data;

import java.util.Map;

public class SerialResultMeta extends SerialObject {
    private String mlName;

    private String modelName;
    
    private Integer returnSize;
    
    private String subType;
    
    private String subSubType;
    
    private Map learnMap;
    
    private Double testAccuracy;
    
    private Double trainAccuracy;
    
    private Map<String, Double[]> classifyMap;
    
    @Deprecated
    private Map countMap;
    
    private Map<String, double[]> offsetMap;
    
    private Double loss;
    
    private Double threshold;
    
    private int size;

    public SerialResultMeta() {
        super();
    }

    public String getMlName() {
        return mlName;
    }

    public void setMlName(String mlName) {
        this.mlName = mlName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getReturnSize() {
        return returnSize;
    }

    public void setReturnSize(Integer returnSize) {
        this.returnSize = returnSize;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSubSubType() {
        return subSubType;
    }

    public void setSubSubType(String subSubType) {
        this.subSubType = subSubType;
    }

    public Map getLearnMap() {
        return learnMap;
    }

    public void setLearnMap(Map learnMap) {
        this.learnMap = learnMap;
    }

    public Double getTestAccuracy() {
        return testAccuracy;
    }

    public void setTestAccuracy(Double testAccuracy) {
        this.testAccuracy = testAccuracy;
    }

    public Double getTrainAccuracy() {
        return trainAccuracy;
    }

    public void setTrainAccuracy(Double trainAccuracy) {
        this.trainAccuracy = trainAccuracy;
    }

    public Map /*<String, Double[]>*/ getClassifyMap() {
        return classifyMap;
    }

    public void setClassifyMap(Map<String, Double[]> classifyMap) {
        this.classifyMap = classifyMap;
    }

    public Map<String, double[]> getOffsetMap() {
        return offsetMap;
    }

    public void setOffsetMap(Map<String, double[]> offsetMap) {
        this.offsetMap = offsetMap;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
}
