package roart.common.pipeline.data;

public class SerialTA extends SerialObject {

    private Integer[] objs;
    
    private double[][] objsarr;    
    
    public SerialTA() {
        super();
    }

    public SerialTA(Integer[] objs, double[][] objsarr) {
        this.objs = objs; 
        this.objsarr = objsarr;    
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

}
