package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import roart.common.config.MLConstants;

@SuppressWarnings("squid:S00116")
public class GemIConfig extends GemConfig {

    private boolean finetune;
    
    private boolean cuda;    

    @JsonCreator
    public GemIConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("n_layers") int n_layers, 
            @JsonProperty("n_hiddens") int n_hiddens, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("finetune") boolean finetune, 
            @JsonProperty("cuda") boolean cuda) {
        super(MLConstants.I, steps, n_layers, n_hiddens, lr);
        this.finetune = finetune;
        this.cuda = cuda;
    }

    public GemIConfig(String name) {
        super(name);
    }

    public boolean isFinetune() {
        return finetune;
    }

    public void setFinetune(boolean finetune) {
        this.finetune = finetune;
    }

    public boolean isCuda() {
        return cuda;
    }

    public void setCuda(boolean cuda) {
        this.cuda = cuda;
    }

    @Override
    public NeuralNetConfig copy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " " + finetune + " " + cuda;
    }
}
