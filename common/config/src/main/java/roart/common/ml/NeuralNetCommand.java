package roart.common.ml;

public class NeuralNetCommand {
    private boolean mllearn;
    
    private boolean mlclassify;
    
    private boolean mldynamic;

    private boolean mlcross;
    
    public boolean isMllearn() {
        return mllearn;
    }

    public void setMllearn(boolean mllearn) {
        this.mllearn = mllearn;
    }

    public boolean isMlclassify() {
        return mlclassify;
    }

    public void setMlclassify(boolean mlclassify) {
        this.mlclassify = mlclassify;
    }

    public boolean isMldynamic() {
        return mldynamic;
    }

    public void setMldynamic(boolean mldynamic) {
        this.mldynamic = mldynamic;
    }

    public boolean isMlcross() {
        return mlcross;
    }

    public void setMlcross(boolean mlcross) {
        this.mlcross = mlcross;
    }
    
}
