package roart.calculate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import roart.gene.impl.CalcComplexGene;

public class CalcMACDNodeTest {

    @Test
    public void aTest() throws JacksonException {
        CalcComplexGene i = new CalcComplexGene();
        i.randomize();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(i));
        System.out.println(i);
    }
}
