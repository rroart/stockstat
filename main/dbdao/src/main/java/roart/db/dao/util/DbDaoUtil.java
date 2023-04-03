package roart.db.dao.util;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.db.dao.DbDao;
import roart.stockutil.MetaDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDaoUtil {

    private static Logger log = LoggerFactory.getLogger(DbDaoUtil.class);

    /**
     * Get the period field text based on the eventual metadata
     * 
     * @return the period text fields
     * @param market
     * @param dbDao 
     */
    
    public static String[] getPeriodText(String market, MyMyConfig conf, DbDao dbDao) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6", "Period7", "Period8", "Period9" };
        MetaItem meta = null;
        try {
            meta = dbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        try {
            if (meta != null) {
                for (int i = 0; i < Constants.PERIODS; i++) {
                    if (meta.getPeriod(i) != null) {
                        periodText[i] = meta.getPeriod(i);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return periodText;
    }

    // ?
    @Deprecated
    public static MetaItem getMeta(String market, MyMyConfig conf) {
        MetaItem meta = null;
        try {
            //meta = DbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return meta;
    }
    
}
