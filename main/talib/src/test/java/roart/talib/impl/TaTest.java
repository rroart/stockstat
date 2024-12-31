package roart.talib.impl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roart.common.pipeline.data.SerialTA;
import roart.common.util.ArraysUtil;
import roart.testdata.TestUtils;
import roart.talib.Ta;

public class TaTest {
    // ta4j: Ta4jSTOCHRSI
    
    @Test
    public void test() {
        double[][] das = getRandomOHLC();

        Ta[][] tas = new Ta[][] {
            { new TalibMACD(), new Ta4jMACD() },
            { new TalibATR(), new Ta4jATR() },
            { new TalibCCI(), new Ta4jCCI() },
            { new TalibMACD(), new Ta4jMACD() },
            { new TalibRSI(), new Ta4jRSI() },
            { new TalibSTOCH(), new Ta4jSTOCH() },
            { new TalibSTOCHRSI(), new Ta4jSTOCHRSI() }
        };

        for (Ta[] ta : tas) {
            System.out.println("ta " + ta[0].getClass().getCanonicalName());
            System.out.println("das" + Arrays.asList(ArraysUtil.convert(das[0])));
            SerialTA talib = ta[0].calculate(das);
            SerialTA ta4j = ta[1].calculate(das);
            System.out.println("talib a " + talib.getBegoffset() + " " + talib.getSize());
            System.out.println("ta4j a " + ta4j.getBegoffset() + " " + ta4j.getSize());
            System.out.println("talib " + talib.getObjs().length + " " + talib.getObjsarr().length);
            System.out.println("ta4j " + ta4j.getObjs().length + " " + ta4j.getObjsarr().length);
            //System.out.println("talib" + talib.getObjs()[3] + " " + talib.getObjsarr()[0][0]);
            //System.out.println("talib" + new ArrayList<>(Arrays.asList(talib.getarray(0))));
            System.out.println("talib " + Arrays.asList(ArraysUtil.convert(talib.getarray(0))));
            System.out.println("ta4j " + Arrays.asList(ArraysUtil.convert(ta4j.getarray(0))));
            System.out.println();
        }
    }

    private double[][] getOHLC() {
        Double[] d = new TestUtils().getNumbersUsingIntStreamRangeDArray(100, 280);
        double[] close = ArraysUtil.convert(d);
        double[] low = new TestUtils().add(close, -0.5);
        double[] high = new TestUtils().add(close, 0.5);
        double[][] das = new double[][] { close, low, high };
        //System.out.println("ar" + Arrays.toL(da));
        System.out.println("ar" + d.length + " " + close.length);
        System.out.println("ar" + das.length + " " + das[0].length);
        return das;
    }

    private double[][] getRandomOHLC() {
        double[] close = new TestUtils().getNumbersRandomDArray(100.0, 180, 0.4);
        double[] low = new TestUtils().getNumbersRandomDArrayAdd(close, -1, 0.5);
        double[] high = new TestUtils().getNumbersRandomDArrayAdd(close, 1, 0.5);
        double[][] das = new double[][] { close, low, high };
        //System.out.println("ar" + Arrays.toL(da));
        System.out.println("ar" + close.length + " " + close.length);
        System.out.println("ar" + das.length + " " + das[0].length);
        return das;
    }
}
