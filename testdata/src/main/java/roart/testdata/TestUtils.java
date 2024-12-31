package roart.testdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import roart.common.constants.CommunicationConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.stereotype.Component;

//@SpringJUnitConfig
//@TestPropertySource("file:${user.dir}/../config/test/application.properties") 
@Component
public class TestUtils {
    
    @Autowired
    private TestConfiguration config;
    
    public Map<String, String> createServiceMap(String value) {
        Map<String, String> map = new HashMap<>();
        map.put(ServiceConstants.EVOLVEFILTERABOVEBELOW, value);
        map.put(ServiceConstants.EVOLVEFILTEREVOLVE, value);
        map.put(ServiceConstants.EVOLVEFILTERFILTER, value);
        map.put(ServiceConstants.EVOLVEFILTERPROFIT, value);
        map.put(ServiceConstants.SIMAUTO, value);
        map.put(ServiceConstants.SIMFILTER, value);
        return map;
    }
    
    public String createServicesAsString(String value) {
        return JsonUtil.convert(createServiceMap(value));
    }

    public Map<String, String> createCommunicationMap() {
        Map<String, String> map = new HashMap<>();
        map.put(CommunicationConstants.CAMEL, config.getServerCamel());
        map.put(CommunicationConstants.KAFKA, config.getServerKafka());
        map.put(CommunicationConstants.PULSAR, config.getServerPulsar());
        map.put(CommunicationConstants.SPRING, config.getServerSpring());
        return map;
    }

    public String createCommunicationAsString() {
        return JsonUtil.convert(createCommunicationMap());
    }

    public List<Integer> getNumbersUsingIntStreamRange(int start, int end) {
        return IntStream.range(start, end)
          .boxed()
          .toList();
    }

    public Double[] getNumbersUsingIntStreamRangeDArray(int start, int end) {
        return IntStream.range(start, end)
          .boxed()
          .map(e -> Double.valueOf(e))
          .toList()
          .toArray(new Double[0]);
    }
    
    public double[] add(double[] array, double value) {
        double[] newarray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            newarray[i] = array[i] + value;
        }
        return newarray;
    }

    Random rand = new Random();
    
    public double[] getNumbersRandomDArray(double start, int count, double range) {
        double[] arr = new double[count];
        arr[0] = start;
        for (int i = 1; i < count; i++) {
            arr[i] = arr[i-1] * (1 + range/2 - rand.nextDouble(range));
        }
        return arr;
    }
    
    public double[] getNumbersRandomDArrayAdd(double[] arr, int mult, double range) {
        double[] newarr = new double[arr.length];
        for (int i = 1; i < arr.length; i++) {
            newarr[i] = arr[i] + mult * rand.nextDouble(range);
        }
        return newarr;
    }
    
}
