package roart.calculate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.gene.impl.CalcComplexGene;

import static org.junit.Assert.*;

public class CalcMACDNodeTest {

    @Test
    public void aTest() throws JsonProcessingException {
        CalcComplexGene i = new CalcComplexGene();
        i.randomize();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(i));
        System.out.println(i);
    }
}
