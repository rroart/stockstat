package roart.common.pipeline.data;

public class SerialDouble extends SerialObject {
    public Double adouble;

    public Double getAdouble() {
        return adouble;
    }

    public SerialDouble(Double adouble) {
        super();
        this.adouble = adouble;
    }

    public void setAdouble(Double adouble) {
        this.adouble = adouble;
    }

    public SerialDouble() {
        super();
    }
    
}
