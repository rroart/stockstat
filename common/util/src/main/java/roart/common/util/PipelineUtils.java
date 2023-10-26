package roart.common.util;

import java.util.HashMap;
import java.util.Map;

import roart.common.pipeline.data.PipelineData;

public class PipelineUtils {
    public static Map<String, PipelineData> getPipelineMap(PipelineData[] datareaders) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            pipelineMap.put(datareader.getName(), datareader);
        }
        return pipelineMap;
    }

    public static Map<String, PipelineData> getPipelineMapStartsWith(PipelineData[] datareaders, String startsWith) {
        Map<String, PipelineData> pipelineMap = new HashMap<>();
        for (PipelineData datareader : datareaders) {
            if (datareader.getName().startsWith(startsWith)) {
                pipelineMap.put(datareader.getName(), datareader);
            }
        }
        return pipelineMap;
    }

    public static PipelineData getPipeline(PipelineData[] datareaders, String name) {
        for (PipelineData datareader : datareaders) {
            if (name.equals(datareader.getName())) {
                return datareader;
            }
        }
        return null;
    }

}
