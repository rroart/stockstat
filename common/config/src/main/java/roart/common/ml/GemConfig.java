package roart.common.ml;

@SuppressWarnings("squid:S00117")
public abstract class GemConfig extends NeuralNetConfig {

    protected int steps;
    
    protected int n_layers;
    
    protected int n_hiddens;
    
    protected double lr;
    
    protected String data_file = "";
    
    public GemConfig(String name, int steps, int n_layers, int n_hiddens, double lr) {
        super(name);
        this.steps = steps;
        this.n_layers = n_layers;
        this.n_hiddens = n_hiddens;
        this.lr = lr;
    }

    public GemConfig(String name) {
        super(name);
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getN_layers() {
        return n_layers;
    }

    public void setN_layers(int n_layers) {
        this.n_layers = n_layers;
    }

    public int getN_hiddens() {
        return n_hiddens;
    }

    public void setN_hiddens(int n_hiddens) {
        this.n_hiddens = n_hiddens;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    public String getData_file() {
        return data_file;
    }

    public void setData_file(String data_file) {
        this.data_file = data_file;
    }

    @Override
    public NeuralNetConfig copy() {
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " " + steps + " " + n_layers + " " + n_hiddens + " " + lr;
    }

}
