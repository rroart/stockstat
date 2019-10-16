package roart.common.config;

public class MLConstants {
    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;
    public static final int ONEVSREST = 3;
    public static final int LINEARSUPPORTVECTORCLASSIFIER = 4;
    
    public static final String SPARK = "spark";
    public static final String TENSORFLOW = "tensorflow";
    public static final String PYTORCH = "pytorch";
    public static final String GEM = "gem";
    public static final String RANDOM = "random";

    public static final String MLPC = "mlpc";
    public static final String LIR = "lir";
    public static final String LOR = "lor";
    public static final String OVR = "ovr";
    public static final String LSVC = "lsvc";

    public static final String DNN = "dnn";
    public static final String DNNLIC = "dnnlic";
    public static final String DNNLIR = "dnnlir";
    public static final String LIC = "lic";
    public static final String LSTM = "lstm";
    public static final String PREDICTORLSTM = "predictorlstm";
    
    public static final String SINGLE = "single";
    public static final String EWC = "ewc";
    public static final String INDEPENDENT = "independent";
    public static final String MULTIMODAL = "multimodal";
    public static final String ICARL = "icarl";
    
    public static final String S = "s";
    public static final String I = "i";
    public static final String MM = "m";

    public static final String MLP = "mlp";
    public static final String RNN = "rnn";
    public static final String CNN = "cnn";
    public static final String GRU = "gru";
    //public static final String GEM = "GEM";
    
    public static final String SPARKLORCONFIG = "{ \"maxiter\" : 1000, \"tol\" : 1.0E-6 }";
    public static final String SPARKMLPCCONFIG = "{ \"maxiter\" : 1000, \"layers\" : 2, \"tol\" : 1.0E-6, \"hidden\" : 100 }";
    public static final String SPARKOVRCONFIG = "{ \"maxiter\" : 1000, \"tol\" : 1.0E-6, \"fitintercept\" : false }";
    public static final String SPARKLSVCCONFIG = "{ \"maxiter\" : 1000, \"tol\" : 1.0E-6, \"fitintercept\" : false }";
    public static final String TENSORFLOWPREDICTORLSTMCONFIG = "{ \"epochs\": 5, \"windowsize\": 3, \"horizon\": 5 }";
    public static final String GEMSINGLECONFIG = "{ \"name\" : \"single\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"data_file\" : \"\" }";
    public static final String GEMINDEPENDENTCONFIG = "{ \"name\" : \"independent\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"finetune\" : false, \"cuda\" : false, \"data_file\" : \"\" }";
    public static final String GEMMULTIMODALCONFIG = "{ \"name\" : \"multimodal\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"data_file\" : \"\" }";
    public static final String GEMEWCCONFIG = "{ \"name\" : \"EWC\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 10, \"memory_strength\" : 1, \"data_file\" : \"\" }";
    public static final String GEMGEMCONFIG = "{ \"name\" : \"GEM\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 256, \"memory_strength\" : 0.5, \"cuda\" : false, \"data_file\" : \"\" }";
    public static final String GEMICARLCONFIG = "{ \"name\" : \"iCaRL\", \"steps\" : 1000, \"n_layers\" : 2, \"n_hiddens\" : 100, \"lr\" : 0.001, \"n_memories\" : 1280, \"memory_strength\" : 1, \"samples_per_task\" : 10, \"cuda\" : false, \"data_file\" : \"\" }";
    public static final String PYTORCHMLPCONFIG = "{ \"name\" : \"mlp\", \"steps\" : 1000, \"hidden\" : 100, \"layers\": 2, \"lr\" : 0.01 }";
    public static final String PYTORCHRNNCONFIG = "{ \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }";
    public static final String PYTORCHLSTMCONFIG = "{ \"name\" : \"lstm\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }";
    public static final String PYTORCHGRUCONFIG = "{ \"name\" : \"gru\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }";
    public static final String PYTORCHCNNCONFIG = "{ \"name\" : \"cnn\", \"steps\" : 1000, \"stride\" : 1, \"kernelsize\" : 4, \"lr\" : 0.01 }";
    public static final String TENSORFLOWDNNCONFIG = "{ \"name\" : \"dnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\": 2 }";
    public static final String TENSORFLOWLICCONFIG = "{ \"name\" : \"lic\", \"steps\" : 1000 }";
    public static final String TENSORFLOWMLPCONFIG = "{ \"name\" : \"mlp\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01 }";
    public static final String TENSORFLOWRNNCONFIG = "{ \"name\" : \"rnn\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }";
    public static final String TENSORFLOWCNNCONFIG = "{ \"name\" : \"cnn\", \"steps\" : 1000, \"stride\" : 1, \"kernelsize\" : 4, \"dropout\" : 0.5 }";
    public static final String TENSORFLOWLSTMCONFIG = "{ \"name\" : \"lstm\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }";
    public static final String TENSORFLOWGRUCONFIG = "{ \"name\" : \"gru\", \"steps\" : 1000, \"hidden\" : 100, \"layers\" : 2, \"lr\" : 0.01, \"dropout\" : 0, \"dropoutin\" : 0 }";
    public static final String TENSORFLOWLIRCONFIG = "{ \"name\" : \"lir\", \"steps\" : 1000 }";
}
