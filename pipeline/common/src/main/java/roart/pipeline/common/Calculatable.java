package roart.pipeline.common;

public abstract class Calculatable {

    protected int category;

    public int getCategory() {
        return category;
    }

    public abstract Object calculate(double[][] array);
    
    public abstract Object calculate(Double[][] array);

    public abstract Object calculate(scala.collection.Seq[] objArray);
    
}
