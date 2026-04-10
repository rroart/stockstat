package roart.common.pipeline.util;

import org.junit.jupiter.api.Test;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialObject;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.data.SerialString;
import roart.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// by gemini

class PipelineBatchTest {

    private static class MockInmemory extends Inmemory {
        private final Map<String, String> storage = new HashMap<>();

        MockInmemory() {
            super("mock-server");
        }

        @Override
        protected int getLimit() {
            return 1024;
        }

        @Override
        protected String getServer() {
            return "mock-server";
        }

        @Override
        protected void set(String key, String value) {
            storage.put(key, value);
        }

        @Override
        protected String get(String key) {
            return storage.get(key);
        }

        @Override
        protected void del(String key) {
            storage.remove(key);
        }
    }

    @Test
    void testBatchVsNonBatch() {
        MockInmemory inmemory = new MockInmemory();
        String category = "testCategory";
        String pipelineKey = "testKey";
        
        String valueStr0 = "valueBatch0";
        SerialString value0 = new SerialString(valueStr0);
        
        String valueStr1 = "valueBatch1";
        SerialString value1 = new SerialString(valueStr1);

        // 1. Non-batched setup
        SerialPipeline nonBatchedPipeline = new SerialPipeline();
        PipelineData nonBatchedData = new PipelineData(category, pipelineKey, null, value0, true);
        
        InmemoryMessage msgNonBatched = inmemory.send("non-batched-id", value0);
        nonBatchedData.setMessage(JsonUtil.convert(msgNonBatched));
        nonBatchedData.setValue(null);
        nonBatchedData.setLoaded(false);
        nonBatchedPipeline.add(nonBatchedData);

        // 2. Batched setup
        SerialPipeline batchedPipeline = new SerialPipeline();
        
        // Add Batch 0
        PipelineData dataForBatch0 = new PipelineData(category, pipelineKey, null, value0, true);
        InmemoryMessage msgBatch0 = inmemory.send("batched-id-0", value0);
        dataForBatch0.setMessage(JsonUtil.convert(msgBatch0));
        dataForBatch0.setValue(null);
        dataForBatch0.setLoaded(false);
        batchedPipeline.add(dataForBatch0, 0);

        // Add Batch 1
        PipelineData dataForBatch1 = new PipelineData(category, pipelineKey, null, value1, true);
        InmemoryMessage msgBatch1 = inmemory.send("batched-id-1", value1);
        dataForBatch1.setMessage(JsonUtil.convert(msgBatch1));
        dataForBatch1.setValue(null);
        dataForBatch1.setLoaded(false);
        batchedPipeline.add(dataForBatch1, 1);

        // 3. Retrieval and Verification
        
        // Retrieve non-batched
        SerialObject nonBatchedResult = PipelineUtils.getPipelineValue(nonBatchedPipeline, category, pipelineKey, inmemory);
        assertEquals(valueStr0, ((SerialString) nonBatchedResult).getString());

        // Retrieve from batched pipeline - Batch 0
        SerialObject batchedResult0 = PipelineUtils.getPipelineValueBatch(batchedPipeline, category, pipelineKey, 0, inmemory);
        assertEquals(valueStr0, ((SerialString) batchedResult0).getString());

        // Retrieve from batched pipeline - Batch 1
        SerialObject batchedResult1 = PipelineUtils.getPipelineValueBatch(batchedPipeline, category, pipelineKey, 1, inmemory);
        assertEquals(valueStr1, ((SerialString) batchedResult1).getString());
        
        // Cross-comparison
        assertEquals(((SerialString) nonBatchedResult).getString(), ((SerialString) batchedResult0).getString(), 
            "Batch 0 should be identical to non-batched result for same data");
    }
}
