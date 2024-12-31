package roart.common.pipeline.data;

public class SerialTA extends SerialObject {

    // first is offset, second size
    private Integer[] objs;
        
    private double[][] objsarr;    
    
    private Integer begoffset;
    
    private Integer size;

    public SerialTA() {
        super();
    }

    public SerialTA(Integer[] objs, double[][] objsarr) {
        this.objs = objs; 
        this.objsarr = objsarr;    
    }

    public SerialTA(Integer[] objs, double[][] objsarr, int begoffset, int size) {
        this.objs = objs; 
        this.objsarr = objsarr;    
        this.begoffset = begoffset;
        this.size = size;
    }

    public int get(int i) {
        return objs[i];
    }

    public double[] getarray(int i) {
        return objsarr[i];
    }

    public Integer[] getObjs() {
        return objs;
    }

    public void setObjs(Integer[] objs) {
        this.objs = objs;
    }

    public double[][] getObjsarr() {
        return objsarr;
    }

    public void setObjsarr(double[][] objsarr) {
        this.objsarr = objsarr;
    }

    public Integer getBegoffset() {
        return begoffset;
    }

    public void setBegoffset(Integer begoffset) {
        this.begoffset = begoffset;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
