package roart.result.model;

import java.util.Map;

public class ResultMeta {
    private String mlName;

    private String modelName;
    
    private Integer returnSize;
    
    private String subType;
    
    private String subSubType;
    
    private Map learnMap;
    
    private Double testAccuracy;
    
    private Map classifyMap;

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

    public Map getClassifyMap() {
        return classifyMap;
    }

    public void setClassifyMap(Map classifyMap) {
        this.classifyMap = classifyMap;
    }
    
}
