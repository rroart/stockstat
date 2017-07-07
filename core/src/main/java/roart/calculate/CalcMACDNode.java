package roart.calculate;

public class CalcMACDNode extends CalcNode {

double minMutateThresholdRange;
double maxMutateThresholdRange;

double threshold;
boolean useminmaxthreshold;
boolean usethreshold;

boolean divideminmaxthreshold;

// not mutatable

boolean doBuy;

double weight;

boolean changeSignWhole;
//boolean threshMaxValue; //?
//boolean norm;

@Override
public double calc(double val, double minmaxthreshold) {
    // TODO Auto-generated method stub
    double mythreshold = threshold;
    if (useminmaxthreshold) {
        mythreshold = minmaxthreshold;
    }
    double myvalue = val - mythreshold;
    if (divideminmaxthreshold) {
        myvalue = myvalue / minmaxthreshold;
    }
    if (changeSignWhole) {
        myvalue = -myvalue;
    }
    return myvalue * weight;
}
}