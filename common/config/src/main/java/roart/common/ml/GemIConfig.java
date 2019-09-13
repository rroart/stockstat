package roart.common.ml;

import roart.common.config.MLConstants;

public class GemIConfig extends GemConfig {

    private boolean finetune;
    
    private boolean cuda;    

    public GemIConfig(int steps, int layers, int hidden, double lr, boolean finetune, boolean cuda) {
        super(MLConstants.I, steps, layers, hidden, lr);
        this.finetune = finetune;
        this.cuda = cuda;
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

}
