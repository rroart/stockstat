package roart.pipeline.common;

import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.model.PipelineResultData;

public abstract class Calculatable extends PipelineResultData {

    protected int category;

    public int getCategory() {
        return category;
    }

    public abstract SerialTA calculate(double[][] array);
    
    public abstract SerialTA calculate(Double[][] array);

    public abstract SerialTA calculate(scala.collection.Seq[] objArray);
    
}
