package roart.gene.impl;

import java.io.IOException;
import java.util.List;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.common.util.JsonUtil;
import roart.gene.CalcGene;

public class CalcGeneFactory {
    public static CalcGene get(String name, String jsonValue, List<Double>[] macdrsiMinMax, int index, boolean useMax) throws StreamReadException, DatabindException
, IOException {
        CalcComplexGene anode;
        if (name != null && name.equals("Double")) {
            return new CalcDoubleGene();
        }
        if (jsonValue == null) {
            anode = new CalcComplexGene();
        } else {
            /*
            CalcComplexGene cg = new CalcComplexGene();
            cg._class = "bla";
            cg.className = "bbb";
            String j2 = mapper.writeValueAsString(cg);
            System.out.println("nnn" + j2);
            System.out.println("nnn" + jsonValue);
            for (int i = 0; i < 10; i++) {
                System.out.print(" " + j2.charAt(i));
            }
            System.out.println("");
            for (int i = 0; i < 10; i++) {
                System.out.print(" " + jsonValue.charAt(i));
            }
            System.out.println("");
            System.out.println("nnn" + jsonValue.replaceAll("\\\\", ""));
            System.out.println("nnn" + jsonValue.substring(1, jsonValue.length() - 2));
            //System.out.println("nnn" + jsonValue.substring(1, jsonValue.length() - 2).replaceAll("\\", ""));
            anode = mapper.readValue(jsonValue, CalcComplexGene.class);
            */
            anode = JsonUtil.convert(jsonValue, CalcComplexGene.class);
        }
        if (macdrsiMinMax == null) {
            int jj = 0;
        }
        if (index >= macdrsiMinMax.length) {
            int jj = 0;
        }
        List<Double> minmax = macdrsiMinMax[index];
        if (minmax == null || minmax.isEmpty()) {
            int jj = 0;
            minmax.add(0.0);
            minmax.add(0.0);
        }
        double minMutateThresholdRange =  minmax.get(0);
        double maxMutateThresholdRange = minmax.get(1);
        if (minMutateThresholdRange == maxMutateThresholdRange) {
            int jj = 0;
        }
        anode.setMinMutateThresholdRange(minMutateThresholdRange);
        anode.setMaxMutateThresholdRange(maxMutateThresholdRange);
        anode.setUseMax(useMax);
        return anode;
    }
}
