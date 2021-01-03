package roart.simulate;

import java.util.List;

import roart.common.util.MathUtil;

public class StockHistory {
    String date;

    Capital capital;

    Capital sum;

    double resultavg;

    String confidence;

    List<String> stocks;

    String trend;

    @Override
    public String toString() {
        return date + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + confidence + " " + stocks + " " + trend;
    }
}
