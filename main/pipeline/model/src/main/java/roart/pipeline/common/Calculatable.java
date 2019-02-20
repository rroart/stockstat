package roart.pipeline.common;

import roart.common.pipeline.model.PipelineResultData;

public abstract class Calculatable extends PipelineResultData {

    protected int category;

    public int getCategory() {
        return category;
    }

    public abstract Object calculate(double[][] array);
    
    public abstract Object calculate(Double[][] array);

    public abstract Object calculate(scala.collection.Seq[] objArray);
    
}
