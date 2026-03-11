package roart.common.pipeline.data;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SerialPipeline extends SerialObject implements Iterable<PipelineData> {
    private PipelineData[] pipelineData = new PipelineData[0];

    /*
    public void add(List<PipelineData> data) {
        add(data.toArray(new PipelineData[0]));
    }

    public void add(PipelineData[] data) {
        pipelineData = ArrayUtils.addAll(pipelineData, data);
    }

     */

    // TODO check for dups?

    public void add(SerialPipeline data) {
        pipelineData = ArrayUtils.addAll(pipelineData, data.pipelineData);
    }

    public void add(PipelineData datum) {
        pipelineData = ArrayUtils.add(pipelineData, datum);
    }

    @Override
    public Iterator<PipelineData> iterator() {
        return Arrays.asList(pipelineData).iterator();
    }

    public int length() {
        return pipelineData.length;
    }

    public boolean isEmpty() {
        return pipelineData.length == 0;
    }
}
