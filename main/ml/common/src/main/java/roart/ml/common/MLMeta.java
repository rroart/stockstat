package roart.ml.common;

public class MLMeta {
    public Integer dim1;
    
    public Integer dim2;
    
    public Integer dim3;
    
    public Integer finaldim;
    
    public Boolean timeseries;
    
    public Boolean features;
    
    public Boolean classify;
    
    public Boolean predict;
    
    public Boolean also1d;

    public String dimString() {
        return dim1 + "_" + (dim2 != null ? dim2 + "_" : "") + (dim3 != null ? dim3 + "_" : "");
    }
}
