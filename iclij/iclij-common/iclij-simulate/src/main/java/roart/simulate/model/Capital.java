package roart.simulate.model;

import roart.common.util.MathUtil;

public class Capital {

    public double amount;

    public String toString() {
        return "" + new MathUtil().round(amount, 2);
    }
}
