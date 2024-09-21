package roart.talib;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;

public abstract class Ta4j extends Ta {

    protected BarSeries getClosedSeries(double[] values, int size) {
        LocalDate date = LocalDate.now();
        BarSeries series = new BaseBarSeries();
        for (int i = 0; i < size; i++) {
            Num open = null;
            Num high = null;
            Num low = null;
            Num close = DecimalNum.valueOf(values[i]);
            Num volume = null;
            LocalDate aDate = date.minusDays(size - 1 - i);
            ZonedDateTime endTime = aDate.atStartOfDay(ZoneOffset.UTC);
            series.addBar(endTime, open, high, low, close, volume);
        }
        return series;
    }

    protected BarSeries getThreeSeries(double[] close, double low[], double high[], int size) {
        LocalDate date = LocalDate.now();
        BarSeries series = new BaseBarSeries();
        for (int i = 0; i < size; i++) {
            Num openNum = null;
            Num highNum = DecimalNum.valueOf(high[i]);
            Num lowNum = DecimalNum.valueOf(low[i]);
            Num closeNum = DecimalNum.valueOf(close[i]);
            Num volumeNum = null;
            LocalDate aDate = date.minusDays(size - 1 - i);
            ZonedDateTime endTime = aDate.atStartOfDay(ZoneOffset.UTC);
            series.addBar(endTime, openNum, highNum, lowNum, closeNum, volumeNum);
        }
        return series;
    }



}
