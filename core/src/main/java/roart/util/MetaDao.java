package roart.util;

import roart.model.MetaItem;

/**
 * 
 * @author roart
 *
 * A DAO for Metadata
 */

public class MetaDao {
    
    /**
     * Get the string for numbered period
     * 
     * @param meta metadata
     * @param i period
     * @return string for period
     * @throws Exception
     */
    
    public static String getPeriod(MetaItem meta, int i) throws Exception {
    	return meta.getperiod(i);
    }
}
