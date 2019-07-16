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
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;

public abstract class Ta4j extends Ta {

    protected TimeSeries getClosedSeries(double[] values, int size) {
        LocalDate date = LocalDate.now();
        TimeSeries series = new BaseTimeSeries();
        for (int i = 0; i < size; i++) {
            Num open = null;
            Num high = null;
            Num low = null;
            Num close = PrecisionNum.valueOf(values[i]);
            Num volume = null;
            LocalDate aDate = date.minusDays(size - 1 - i);
            ZonedDateTime endTime = aDate.atStartOfDay(ZoneOffset.UTC);
            series.addBar(endTime, open, high, low, close, volume);
        }
        return series;
    }



}
