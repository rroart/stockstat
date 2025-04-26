package roart.talib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.model.StockDTO;
import roart.common.pipeline.data.SerialTA;
import roart.common.util.ArraysUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;

public class TaUtil {

    private static Logger log = LoggerFactory.getLogger(TaUtil.class);

    public int getArr(int days, String market, String id, Set<Pair<String, String>> ids, Integer periodInt,
            List<StockDTO>[] datedstocklists, double[][] arrarr) {
        double[] values = arrarr[0];
        double[] low = null;
        double[] high = null;
        if (arrarr.length == Constants.OHLC) {
            low = arrarr[1];
            high = arrarr[2];
        }
        int size = 0;
        boolean display = false;
        int count = 0;
        int downcount = Math.min(days, datedstocklists.length);
        List<Double> listd = new ArrayList<>();
        List<Double> liste = new ArrayList<>();
        List<Double> listf = new ArrayList<>();
        for (int j = 0; j < datedstocklists.length  && downcount > 0 ; j++) {
            List<StockDTO> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull {} {}", market, j);
                continue;
            }
            if (periodInt == null) {
                continue;
            }
            int period = periodInt;
            grr:  for (int i = 0; i < list.size(); i++) {
                StockDTO stock = list.get(i);
                //System.out.print(" " + stock.getId());
                Pair<String, String> pair = new ImmutablePair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double[] allValues = StockDao.getValue(stock, period);
                        Double value = allValues[0];
                        if (value == null) {
                            display = true;
                            continue;
                        }
                        Double alow = null;
                        Double ahigh = null;
                        if (allValues.length > 1) {
                            alow = allValues[1];
                            ahigh = allValues[2];
                        }
                        double val = value;
                        values[count] = value;
                        if (low != null) {
                            low[count] = alow;
                        }
                        if (high != null) {
                            high[count] = ahigh;
                        }
                        listd.add(val);
                        liste.add(alow);
                        listf.add(ahigh);
                        count++;
                        downcount--;
                        size++;
                        break grr;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }    
        }
        Collections.reverse(listd);
        Collections.reverse(liste);
        Collections.reverse(listf);
        Object[] newarr = listd.toArray();
        Object[] newars = liste.toArray();
        Object[] newart = listf.toArray();
        log.info("arrarr {} {} {} {}", newarr, values, size, newarr.length);
        for (int i = 0; i < size; i ++) {
            values[i] = (double) newarr[i];
        }
        if (low != null) {
            for (int i = 0; i < size; i ++) {
                low[i] = (double) newars[i];
            }
        }
        if (high != null) {
            for (int i = 0; i < size; i ++) {
                high[i] = (double) newart[i];
            }
        }
        //System.arraycopy(newarr, 0, values, 0, size);
        //System.out.println("thearr " + Arrays.toString(values));
        if (display) {
            //log.info("mydisplay " + list);
        }
        return size;
    }

    @Deprecated
    private int getArrForOrig(int days, String market, Set<Pair> ids, Integer periodInt,
            List<StockDTO>[] datedstocklists, double[] values) {
        int size = 0;
        for (int j = days - 1; j >= 0; j--) {
            List<StockDTO> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull " + market + " " + " " + j);
                continue;
            }
            int period = periodInt;
            for (int i = 0; i < list.size(); i++) {
                StockDTO stock = list.get(i);
                Pair<String, String> pair = new ImmutablePair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double value = StockDao.getMainPeriod(stock, period);
                        if (value == null) {
                            continue;
                        }
                        values[size] = value;
                        size++;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);

                    }
                }
            }    
        }
        return size;
    }

    private double getArr(Object[] objs, int arrInd, int endInd) {
        double[] arr = (double[]) objs[arrInd];
        int end = (int) objs[endInd];
        if (end == 0) {
            return 0;
        }
        return arr[end - 1];
    }

    private double getArrDelta(Object[] objs, int arrInd, int endInd, int deltadays) {
        double[] rsi = (double[]) objs[arrInd];
        int end = (int) objs[endInd];
        if (end == 0) {
            return 0;
        }
        double delta = 0;
        int min = Math.max(0, end - deltadays);
        delta = rsi[end - 1] - rsi[min];
        return delta/(deltadays - 1);
    }

    public Double[] getWithOneAndDelta(int rsideltadays, SerialTA objs) {
        return getWithOneAndDelta(rsideltadays, objs, 0);
    }

    public Double[] getWithOneAndDelta(int deltadays, SerialTA objs, int offset) {
        int end = (int) objs.get(TaConstants.ONEIDXEND);
        if (end < offset + deltadays) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[2];
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.ONEIDXARRONE, TaConstants.ONEIDXEND, offset);
        retValues[retindex++] = getArrayValueAtOffsetDelta(objs, TaConstants.ONEIDXARRONE, TaConstants.ONEIDXEND, deltadays, offset);
        return retValues;
    }

    public int getWithOneAndDelta(boolean wantrsidelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 1;
            if (wantrsidelta) {
                retindex++;
            }
            return retindex;
        }
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wantrsidelta) {
            retValues[retindex++] = objs[1];
        }
        return retindex;
    }

    public Double[] getWithTwoAndDelta(int onedeltadays, int twodeltadays, SerialTA objs, int offset) {
        int end = (int) objs.get(TaConstants.TWOIDXEND);
        if (end < offset + Math.max(onedeltadays, twodeltadays)) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[4];
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.TWOIDXARRONE, TaConstants.TWOIDXEND, offset);
        retValues[retindex++] = getArrayValueAtOffsetDelta(objs, TaConstants.TWOIDXARRONE, TaConstants.TWOIDXEND, twodeltadays, offset);
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.TWOIDXARRTWO, TaConstants.TWOIDXEND, offset);
        retValues[retindex] = getArrayValueAtOffsetDelta(objs, TaConstants.TWOIDXARRTWO, TaConstants.TWOIDXEND, twodeltadays, offset);
        return retValues;
    }

    public Double[] getWithTwoAndDelta(int onedeltadays, int twodeltadays, SerialTA objs) {
        return getWithTwoAndDelta(onedeltadays, twodeltadays, objs, 0);
    }

    public int getWithTwoAndDelta(boolean wantonedelta, boolean wanttwodelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 2;
            if (wanttwodelta) {
                retindex++;
            }
            if (wantonedelta) {
                retindex++;
            }
            return retindex;
        }
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wanttwodelta) {
            retValues[retindex++] = objs[1];
        }
        retValues[retindex++] = objs[2];
        if (wantonedelta) {
            retValues[retindex++] = objs[3];
        }
        return retindex;
    }

    public Double[] getWithThreeAndDelta(int onedeltadays, int twodeltadays, int threedeltadays, SerialTA objs, int offset) {
        int end = (int) objs.get(TaConstants.THREEIDXEND);
        int max = Collections.max(Arrays.asList(new Integer[] { onedeltadays, twodeltadays, threedeltadays }));
        if (end < offset + max) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[6];
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.THREEIDXARRONE, TaConstants.THREEIDXEND, offset);
        retValues[retindex++] = getArrayValueAtOffsetDelta(objs, TaConstants.THREEIDXARRONE, TaConstants.THREEIDXEND, onedeltadays, offset);
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.THREEIDXARRTWO, TaConstants.THREEIDXEND, offset);
        retValues[retindex++] = getArrayValueAtOffsetDelta(objs, TaConstants.THREEIDXARRTWO, TaConstants.THREEIDXEND, twodeltadays, offset);
        retValues[retindex++] = getArrayValueAtOffset(objs, TaConstants.THREEIDXARRTHREE, TaConstants.THREEIDXEND, offset);
        retValues[retindex] = getArrayValueAtOffsetDelta(objs, TaConstants.THREEIDXARRTHREE, TaConstants.THREEIDXEND, threedeltadays, offset);
        return retValues;
    }

    public Double[] getWithThreeAndDelta(int onedeltadays, int twodeltadays, int threedeltadays, SerialTA objs) {
        return getWithThreeAndDelta(onedeltadays, twodeltadays, threedeltadays, objs, 0);
    }

    public int getWithThreeAndDelta(boolean wantonedelta, boolean wanttwodelta, boolean wantthreedelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 3;
            if (wantonedelta) {
                retindex++;
            }
            if (wanttwodelta) {
                retindex++;
            }
            if (wantthreedelta) {
                retindex++;
            }
            return retindex;
        }
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wantonedelta) {
            retValues[retindex++] = objs[1];
        }
        retValues[retindex++] = objs[2];
        if (wanttwodelta) {
            retValues[retindex++] = objs[3];
        }
        retValues[retindex++] = objs[4];
        if (wantthreedelta) {
            retValues[retindex++] = objs[5];
        }
        return retindex;
    }

    private Double getArrayValueAtOffset(SerialTA objs, int arrayindex, int endvalueindex, int offset) {
        double[] hist = (double[]) objs.getarray(arrayindex);
        int end = (int) objs.get(endvalueindex);
        if (end == 0) {
            return null;
        }
        return hist[end - offset - 1];
    }
    
    private Double getArrayValueAtOffsetDelta(SerialTA objs, int arrayindex, int endvalueindex, int deltadays, int offset) {
        double[] hist = (double[]) objs.getarray(arrayindex);
        int end = (int) objs.get(endvalueindex);
        if (end == 0) {
            return null;
        }
        double delta = 0;
        int min = Math.max(0, end - offset - deltadays);
        delta = hist[end - offset - 1] - hist[min];
        return delta/(deltadays - 1);
    }


}
