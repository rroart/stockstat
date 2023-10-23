package roart.stockutil;

import roart.common.constants.CategoryConstants;
import roart.common.model.MetaItem;
import roart.model.data.MarketData;

public class MetaUtil {

    public static boolean currentYear(MetaItem meta, String categoryTitle) {
        if (meta == null) {
            return false;
        }
        String reset = meta.getReset();
        if (reset == null) {
            return false;
        }
        String[] resets = reset.split(",");
        for (String aReset : resets) {
            int rest = aReset.indexOf('(');
            String metaCategoryTitle = aReset.substring(0, rest);
            String period = aReset.substring(rest);
            // only one supported
            if (!"(y)".equals(period)) {
                continue;
            }
            if (metaCategoryTitle.equals(categoryTitle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean normalPeriod(MarketData marketdata, int period, String title) {
        if (period < 0) {
            return true;
        }
        if (CategoryConstants.INDEX.equals(title) || CategoryConstants.PRICE.equals(title)) {
            return true;
        }
        return currentYear(marketdata.meta, title);
    }
}
