package roart.ml.model;

import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowDNNLConfig;
import roart.common.ml.TensorflowLConfig;

public class LearnTestClassify {
    private TensorflowDNNConfig tensorflowDNNConfig;
    
    private TensorflowDNNLConfig tensorflowDNNLConfig;
    
    private TensorflowLConfig tensorflowLConfig;
    
    private Object[][] trainingarray;

    private Object[] trainingcatarray;
    
    private Object[][] classifyarray;

    private Object[] classifycatarray;

    private Object[] classifyprobarray;

    private int modelInt;
    
    private String modelName;
    
    private String filename;
    
    private String path;
    
    private int size;
    
    private String mapname;
    
    private int classes;
    
    private Double accuracy;

    public TensorflowDNNConfig getTensorflowDNNConfig() {
        return tensorflowDNNConfig;
    }

    public void setTensorflowDNNConfig(TensorflowDNNConfig tensorflowDNNConfig) {
        this.tensorflowDNNConfig = tensorflowDNNConfig;
    }

    public TensorflowDNNLConfig getTensorflowDNNLConfig() {
        return tensorflowDNNLConfig;
    }

    public void setTensorflowDNNLConfig(TensorflowDNNLConfig tensorflowDNNLConfig) {
        this.tensorflowDNNLConfig = tensorflowDNNLConfig;
    }

    public TensorflowLConfig getTensorflowLConfig() {
        return tensorflowLConfig;
    }

    public void setTensorflowLConfig(TensorflowLConfig tensorflowLConfig) {
        this.tensorflowLConfig = tensorflowLConfig;
    }

    public LearnTestClassify() {
        super();
    }

    public Object[][] getTrainingarray() {
        return trainingarray;
    }

    public void setTrainingarray(Object[][] trainingarray) {
        this.trainingarray = trainingarray;
    }

    public Object[] getTrainingcatarray() {
        return trainingcatarray;
    }

    public void setTrainingcatarray(Object[] catarray) {
        this.trainingcatarray = catarray;
    }

    public Object[][] getClassifyarray() {
        return classifyarray;
    }

    public void setClassifyarray(Object[][] classifyarray) {
        this.classifyarray = classifyarray;
    }

    public Object[] getClassifycatarray() {
        return classifycatarray;
    }

    public void setClassifycatarray(Object[] classifycatarray) {
        this.classifycatarray = classifycatarray;
    }

    public Object[] getClassifyprobarray() {
        return classifyprobarray;
    }

    public void setClassifyprobarray(Object[] classifyprobarray) {
        this.classifyprobarray = classifyprobarray;
    }

    public int getModelInt() {
        return modelInt;
    }

    public void setModelInt(int modelInt) {
        this.modelInt = modelInt;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMapname() {
        return mapname;
    }

    public void setMapname(String mapname) {
        this.mapname = mapname;
    }

    public int getClasses() {
        return classes;
    }

    public void setClasses(int classes) {
        this.classes = classes;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

}
