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
    // TODO ta4j only: Ta4jSTOCHRSI
    
    @Test
    public void test() {
        double[][] das = getRandomOHLC();

        Ta[][] tas = getTAs();

        printTAs(das, tas);
    }

    @Test
    public void test2() {
        double[][] das = getCustom();
        
        Ta[][] tas = getTAs();
/*
        tas = new Ta[][] {
            { new TalibSTOCHRSI(), new Ta4jSTOCHRSI() }
        };

        tas = new Ta[][] {
            { new TalibRSI(), new Ta4jRSI() }
        };
*/
        printTAs(das, tas);
    }

    private void printTAs(double[][] das, Ta[][] tas) {
        System.out.println("das" + Arrays.asList(ArraysUtil.convert(das[0])));
        for (Ta[] ta : tas) {
            System.out.println("ta " + ta[0].getClass().getCanonicalName());
            SerialTA talib = ta[0].calculate(das);
            SerialTA ta4j = ta[1].calculate(das);
            System.out.println("talib a " + talib.getBegoffset() + " " + talib.getSize());
            System.out.println("ta4j a " + ta4j.getBegoffset() + " " + ta4j.getSize());
            System.out.println("talib " + talib.getObjs().length + " " + talib.getObjsarr().length);
            System.out.println("ta4j " + ta4j.getObjs().length + " " + ta4j.getObjsarr().length);
            //System.out.println("talib" + talib.getObjs()[3] + " " + talib.getObjsarr()[0][0]);
            //System.out.println("talib" + new ArrayList<>(Arrays.asList(talib.getarray(0))));
            for (int i = 0; i < 4; i++) {
                if (talib.getarray(i) != null) {
                    //System.out.println("talib " + talib.getarray(i).getClass().getCanonicalName());
                }
                if (talib.getarray(i) == null || ta4j.getarray(i) == null /*|| talib.getarray(i).getClass() == null*/) {
                    break;
                }
                System.out.println("talib " + Arrays.asList(ArraysUtil.convert(talib.getarray(i))));
                System.out.println("ta4j " + Arrays.asList(ArraysUtil.convert(ta4j.getarray(i))));
            }
            System.out.println();
        }
    }

    private Ta[][] getTAs() {
        Ta[][] tas = new Ta[][] {
            { new TalibATR(), new Ta4jATR() },
            { new TalibCCI(), new Ta4jCCI() },
            { new TalibMACD(), new Ta4jMACD() },
            { new TalibRSI(), new Ta4jRSI() },
            { new TalibSTOCH(), new Ta4jSTOCH() },
            { new TalibSTOCHRSI(), new Ta4jSTOCHRSI() }
        };
        return tas;
    }

    private double[][] getCustom() {
        double[][] das;
        double[] da = new double[] { 50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07, 50.01, 50.14, 50.22,
                50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62, 50.90, 50.82, 50.86, 51.20, 51.30, 51.10,  
                50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07, 50.01, 50.14, 50.22,
                50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62, 50.90, 50.82, 50.86, 51.20, 51.30, 51.10};
        das = new double[][] { da, da, da };
        return das;
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
