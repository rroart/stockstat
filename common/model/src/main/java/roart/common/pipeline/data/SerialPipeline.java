package roart.common.pipeline.data;

import org.apache.commons.lang3.ArrayUtils;
import roart.common.util.JsonUtil;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.jdk.JDKKeySerializers;

import java.util.*;

public class SerialPipeline extends SerialObject implements Iterable<PipelineData> {

    //@JsonSerialize(keyUsing = MyCustomSerializer.class)
    @JsonDeserialize(keyUsing = SerialPipelineKeyDeserializer.class)
@JsonSerialize(keyUsing = SerialPipelineKeySerialiser.class)
    private Map<SerialPipelineKey, PipelineData> pipeMap = new HashMap<>();

    // for jackson
    public Map<SerialPipelineKey, PipelineData> getPipeMap() {
        return pipeMap;
    }

    // for jackson
    public void setPipeMap(Map<SerialPipelineKey, PipelineData> pipeMap) {
        this.pipeMap = pipeMap;
    }
/*
    public PipelineData[] getPipelineData() {
        return pipeMap.values().toArray(new PipelineData[0];
    }

    public void setPipelineData(PipelineData[] pipelineData) {
        this.pipelineData = pipelineData;
    }

     */
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
        for (PipelineData datum : data) {
            if (keyExists(datum.getKey())) {
                log.error("Key exists {}", ArrayUtils.toString(datum.getKey()));
                try {
                    String s = null;
                    s.length();
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }
        pipeMap.putAll(data.pipeMap);
    }

    public void add(PipelineData datum) {
        if (keyExists(datum.getKey())) {
            log.error("Key exists {}", ArrayUtils.toString(datum.getKey()));
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
        pipeMap.put(datum.getKey(), datum);
    }

    public void add(PipelineData datum, int batch) {
        if (batch == 0 && keyExists(datum.getKey())) {
            log.error("Key exists {}", ArrayUtils.toString(datum.getKey()));
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
        if (batch == 0) {
            pipeMap.put(datum.getKey(), datum);
            datum.add(datum, batch);
            //dataMap.put(datum.getKey(), datum);
        } else {
            PipelineData d = pipeMap.get(datum.getKey());
            d.add(datum, batch);
            //PipelineData data = get(datum.getKey());
            //data.add();
        }
    }

    public boolean keyExists(SerialPipelineKey key) {
        return pipeMap.keySet().contains(key);
    }

    @Override
    public Iterator<PipelineData> iterator() {
        return pipeMap.values().iterator();
    }

    public int length() {
        return pipeMap.size();
    }

    public boolean isEmpty() {
        return pipeMap.isEmpty();
    }

    private static class SerialPipelineKeySerialiser extends JDKKeySerializers.StringKeySerializer {
        public SerialPipelineKeySerialiser() {
            // for jackson
        }

        @Override
        public void serialize(Object value, JsonGenerator g, SerializationContext provider) {
            // unnecessary assignment for readability
            String newKeyValue = JsonUtil.convert(value);
            g.writeName(newKeyValue);
        }
    }

    private static class SerialPipelineKeyDeserializer extends KeyDeserializer {
        public SerialPipelineKeyDeserializer() {
            // for jackson
        }

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return JsonUtil.convertnostrip(key, SerialPipelineKey.class);
        }
    }

}
