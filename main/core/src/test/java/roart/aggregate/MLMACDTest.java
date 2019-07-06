package roart.aggregate;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import roart.aggregator.impl.IndicatorAggregator;
import roart.aggregator.impl.MLMACD;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MLMACDTest {
	@Test
	public void test() throws Exception {	
		String type = "t";
		Map<String, Map<String, String>> map = new HashMap<>();
		Map<String, String> getMap = MLMACD.mapGetterOrig(map, type);
		assertNotNull(getMap);
		Map<String, String> getMap2 = MLMACD.mapGetterOrig(map, type);
		assertEquals(getMap, getMap2);
		Map<String, String> getMap3 = IndicatorAggregator.mapGetter(map, type);
		assertNotNull(getMap3);
		Map<String, String> getMap4 = IndicatorAggregator.mapGetter(map, type);
		assertEquals(getMap3, getMap4);
	}
}
