package roart.ml.model;

import roart.common.ml.GemEWCConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.GemMMConfig;
import roart.common.ml.GemSConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.PytorchCNN2Config;
import roart.common.ml.PytorchCNNConfig;
import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchRNNConfig;
import roart.common.ml.TensorflowCNN2Config;
import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowConditionalGANConfig;
import roart.common.ml.TensorflowDCGANConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowGRUConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.common.ml.TensorflowLIRConfig;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.ml.TensorflowMLPConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.ml.TensorflowRNNConfig;

public class LearnTestClassify {
    
    private TensorflowDNNConfig tensorflowDNNConfig;
    
    private TensorflowLICConfig tensorflowLICConfig;
    
    private TensorflowLIRConfig tensorflowLIRConfig;

    private TensorflowPredictorLSTMConfig tensorflowPredictorLSTMConfig;

    private TensorflowMLPConfig tensorflowMLPConfig;
    
    private TensorflowCNNConfig tensorflowCNNConfig;
    
    private TensorflowCNN2Config tensorflowCNN2Config;
    
    private TensorflowRNNConfig tensorflowRNNConfig;
    
    private TensorflowLSTMConfig tensorflowLSTMConfig;
    
    private TensorflowGRUConfig tensorflowGRUConfig;
    
    private TensorflowDCGANConfig tensorflowDCGANConfig;
    
    private TensorflowConditionalGANConfig tensorflowConditionalGANConfig;
    
    private PytorchMLPConfig pytorchMLPConfig;
    
    private PytorchCNNConfig pytorchCNNConfig;
    
    private PytorchCNN2Config pytorchCNN2Config;
    
    private PytorchRNNConfig pytorchRNNConfig;
    
    private PytorchLSTMConfig pytorchLSTMConfig;
    
    private PytorchGRUConfig pytorchGRUConfig;
    
    private GemSConfig gemSConfig;
    
    private GemIConfig gemIConfig;
    
    private GemMMConfig gemMMConfig;
    
    private GemEWCConfig gemEWCConfig;
    
    private GemGEMConfig gemGEMConfig;
    
    private GemIcarlConfig gemIcarlConfig;
    
    private Object[] trainingarray;

    private Object[] trainingcatarray;
    
    private Object[] classifyarray;

    private Object[] classifycatarray;

    private Object[] classifyprobarray;

    private int modelInt;
    
    private String modelName;
    
    private String filename;
    
    private String path;
    
    private NeuralNetCommand neuralnetcommand;
    
    private int size;
    
    private String mapname;
    
    private int classes; // used for number of predicted classes, number of predicted sequence or generated words
    
    private Double accuracy;

    private Double trainaccuracy;

    private Double loss;
    
    private Boolean exists;
   
    private String dataset;
    
    private Boolean classify;
    
    private Boolean zero;
    
    private Boolean gpu;
    
    private Boolean memory;
    
    private Boolean exception;
    
    private Boolean cudnn;
    
    public TensorflowDNNConfig getTensorflowDNNConfig() {
        return tensorflowDNNConfig;
    }

    public void setTensorflowDNNConfig(TensorflowDNNConfig tensorflowDNNConfig) {
        this.tensorflowDNNConfig = tensorflowDNNConfig;
    }

    public TensorflowLICConfig getTensorflowLICConfig() {
        return tensorflowLICConfig;
    }

    public void setTensorflowLICConfig(TensorflowLICConfig tensorflowLICConfig) {
        this.tensorflowLICConfig = tensorflowLICConfig;
    }

    public TensorflowLIRConfig getTensorflowLIRConfig() {
        return tensorflowLIRConfig;
    }

    public void setTensorflowLIRConfig(TensorflowLIRConfig tensorflowLIRConfig) {
        this.tensorflowLIRConfig = tensorflowLIRConfig;
    }

    public TensorflowPredictorLSTMConfig getTensorflowPredictorLSTMConfig() {
        return tensorflowPredictorLSTMConfig;
    }

    public void setTensorflowPredictorLSTMConfig(TensorflowPredictorLSTMConfig tensorflowPredictorLSTMConfig) {
        this.tensorflowPredictorLSTMConfig = tensorflowPredictorLSTMConfig;
    }

    public TensorflowMLPConfig getTensorflowMLPConfig() {
        return tensorflowMLPConfig;
    }

    public void setTensorflowMLPConfig(TensorflowMLPConfig tensorflowMLPConfig) {
        this.tensorflowMLPConfig = tensorflowMLPConfig;
    }

    public TensorflowCNNConfig getTensorflowCNNConfig() {
        return tensorflowCNNConfig;
    }

    public void setTensorflowCNNConfig(TensorflowCNNConfig tensorflowCNNConfig) {
        this.tensorflowCNNConfig = tensorflowCNNConfig;
    }

    public TensorflowCNN2Config getTensorflowCNN2Config() {
        return tensorflowCNN2Config;
    }

    public void setTensorflowCNN2Config(TensorflowCNN2Config tensorflowCNN2Config) {
        this.tensorflowCNN2Config = tensorflowCNN2Config;
    }

    public TensorflowRNNConfig getTensorflowRNNConfig() {
        return tensorflowRNNConfig;
    }

    public void setTensorflowRNNConfig(TensorflowRNNConfig tensorflowRNNConfig) {
        this.tensorflowRNNConfig = tensorflowRNNConfig;
    }

    public TensorflowLSTMConfig getTensorflowLSTMConfig() {
        return tensorflowLSTMConfig;
    }

    public void setTensorflowLSTMConfig(TensorflowLSTMConfig tensorflowLSTMConfig) {
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }

    public TensorflowGRUConfig getTensorflowGRUConfig() {
        return tensorflowGRUConfig;
    }

    public void setTensorflowGRUConfig(TensorflowGRUConfig tensorflowGRUConfig) {
        this.tensorflowGRUConfig = tensorflowGRUConfig;
    }

    public PytorchMLPConfig getPytorchMLPConfig() {
        return pytorchMLPConfig;
    }

    public void setPytorchMLPConfig(PytorchMLPConfig pytorchMLPConfig) {
        this.pytorchMLPConfig = pytorchMLPConfig;
    }

    public PytorchCNNConfig getPytorchCNNConfig() {
        return pytorchCNNConfig;
    }

    public void setPytorchCNNConfig(PytorchCNNConfig pytorchCNNConfig) {
        this.pytorchCNNConfig = pytorchCNNConfig;
    }

    public PytorchCNN2Config getPytorchCNN2Config() {
        return pytorchCNN2Config;
    }

    public void setPytorchCNN2Config(PytorchCNN2Config pytorchCNN2Config) {
        this.pytorchCNN2Config = pytorchCNN2Config;
    }

    public PytorchRNNConfig getPytorchRNNConfig() {
        return pytorchRNNConfig;
    }

    public void setPytorchRNNConfig(PytorchRNNConfig pytorchRNNConfig) {
        this.pytorchRNNConfig = pytorchRNNConfig;
    }

    public PytorchLSTMConfig getPytorchLSTMConfig() {
        return pytorchLSTMConfig;
    }

    public void setPytorchLSTMConfig(PytorchLSTMConfig pytorchLSTMConfig) {
        this.pytorchLSTMConfig = pytorchLSTMConfig;
    }

    public PytorchGRUConfig getPytorchGRUConfig() {
        return pytorchGRUConfig;
    }

    public void setPytorchGRUConfig(PytorchGRUConfig pytorchGRUConfig) {
        this.pytorchGRUConfig = pytorchGRUConfig;
    }

    public GemSConfig getGemSConfig() {
        return gemSConfig;
    }

    public void setGemSConfig(GemSConfig gemSConfig) {
        this.gemSConfig = gemSConfig;
    }

    public GemIConfig getGemIConfig() {
        return gemIConfig;
    }

    public void setGemIConfig(GemIConfig gemIConfig) {
        this.gemIConfig = gemIConfig;
    }

    public GemMMConfig getGemMMConfig() {
        return gemMMConfig;
    }

    public void setGemMMConfig(GemMMConfig gemMConfig) {
        this.gemMMConfig = gemMConfig;
    }

    public GemEWCConfig getGemEWCConfig() {
        return gemEWCConfig;
    }

    public void setGemEWCConfig(GemEWCConfig gemEWCConfig) {
        this.gemEWCConfig = gemEWCConfig;
    }

    public GemGEMConfig getGemGEMConfig() {
        return gemGEMConfig;
    }

    public void setGemGEMConfig(GemGEMConfig gemGEMConfig) {
        this.gemGEMConfig = gemGEMConfig;
    }

    public GemIcarlConfig getGemIcarlConfig() {
        return gemIcarlConfig;
    }

    public void setGemIcarlConfig(GemIcarlConfig gemIcarlConfig) {
        this.gemIcarlConfig = gemIcarlConfig;
    }

    public LearnTestClassify() {
        super();
    }

    public Object[] getTrainingarray() {
        return trainingarray;
    }

    public void setTrainingarray(Object[] trainingarray) {
        this.trainingarray = trainingarray;
    }

    public Object[] getTrainingcatarray() {
        return trainingcatarray;
    }

    public void setTrainingcatarray(Object[] catarray) {
        this.trainingcatarray = catarray;
    }

    public Object[] getClassifyarray() {
        return classifyarray;
    }

    public void setClassifyarray(Object[] classifyarray) {
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

    public NeuralNetCommand getNeuralnetcommand() {
        return neuralnetcommand;
    }

    public void setNeuralnetcommand(NeuralNetCommand neuralnetcommand) {
        this.neuralnetcommand = neuralnetcommand;
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

    public Double getTrainaccuracy() {
        return trainaccuracy;
    }

    public void setTrainaccuracy(Double trainaccuracy) {
        this.trainaccuracy = trainaccuracy;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public Boolean getClassify() {
        return classify;
    }

    public void setClassify(Boolean classify) {
        this.classify = classify;
    }

    public Boolean getZero() {
        return zero;
    }

    public void setZero(Boolean zero) {
        this.zero = zero;
    }

    public Boolean getGpu() {
        return gpu;
    }

    public void setGpu(Boolean gpu) {
        this.gpu = gpu;
    }

    public Boolean getMemory() {
        return memory;
    }

    public void setMemory(Boolean memory) {
        this.memory = memory;
    }

    public Boolean getException() {
        return exception;
    }

    public void setException(Boolean exception) {
        this.exception = exception;
    }

    public Boolean getCudnn() {
        return cudnn;
    }

    public void setCudnn(Boolean cudnn) {
        this.cudnn = cudnn;
    }

}
