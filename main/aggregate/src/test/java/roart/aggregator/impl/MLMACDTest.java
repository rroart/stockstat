package roart.aggregator.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import roart.aggregator.impl.IndicatorAggregator;
import roart.aggregator.impl.MLMACD;

import static org.junit.jupiter.api.Assertions.*;

public class MLMACDTest {
	@Test
	public void test() throws Exception {	
		String type = "t";
		Map<String, Map<String, String>> map = new HashMap<>();
		Map<String, String> getMap = MLMACD.mapGetter(map, type);
		assertNotNull(getMap);
		Map<String, String> getMap2 = MLMACD.mapGetter(map, type);
		assertEquals(getMap, getMap2);
		Map<String, String> getMap3 = IndicatorAggregator.mapGetter(map, type);
		assertNotNull(getMap3);
		Map<String, String> getMap4 = IndicatorAggregator.mapGetter(map, type);
		assertEquals(getMap3, getMap4);
	}
}
