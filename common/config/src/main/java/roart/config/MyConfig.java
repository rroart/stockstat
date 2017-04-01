package roart.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MyConfig {

    protected static Logger log = LoggerFactory.getLogger(MyConfig.class);
    
    protected static MyConfig instance = null;
    
    public static MyConfig instance() {
        return instance;
    }
    
    public MyConfig() {
    }
    
    public boolean useSpark = false;
    
    public String sparkMaster = null;

	private Date mydate = null;

	private String mymarket = "0";

	private Integer mydays = 180;

	private Integer mytopbottom = 10;

	private Integer mytabledays = 180;

	private Integer mytablemoveintervaldays = 5;

	private Integer mytableintervaldays = 1;

	private boolean myequalize = false;

	private boolean mygraphequalize = false;

	private boolean mygraphequalizeunify = false;

	private boolean macdEnabled = true;

	private boolean macdDeltaEnabled = true;

	private boolean macdHistogramDeltaEnabled = true;

	private boolean moveEnabled = true;

	private boolean rsiEnabled = true;
    
	private boolean rsiDeltaEnabled = true;
    
	private int macdDeltaDays = 3;

	private int macdHistogramDeltaDays = 3;

	private int rsiDeltaDays = 3;

    public abstract void config() throws Exception;

	/**
	 * Set current date
	 * 
	 * @param date
	 */
	
	public void setdate(Date date) {
	    mydate = date;
	}

	/**
	 * Get current date
	 * 
	 * @return date
	 */
	
	public Date getdate() {
	    return mydate;
	}

	public void setMarket(String value) {
	    mymarket = value;
	}

	public String getMarket() {
	    return mymarket;
	}

	public void setDays(Integer integer) {
	    mydays = integer;
	}

	public int getDays() {
	    return mydays;
	}

	public void setTopBottom(Integer integer) {
	    mytopbottom = integer;
	}

	public int getTopBottom() {
	    return mytopbottom;
	}

	public void setTableDays(Integer integer) {
	    mytabledays = integer;
	}

	public int getTableDays() {
	    return mytabledays;
	}

	public void setTableIntervalDays(Integer integer) {
	    mytableintervaldays = integer;
	}

	public int getTableIntervalDays() {
	    return mytableintervaldays;
	}

	public void setTableMoveIntervalDays(Integer integer) {
	    mytablemoveintervaldays = integer;
	}

	public int getTableMoveIntervalDays() {
	    return mytablemoveintervaldays;
	}

	public void setEqualize(Boolean integer) {
	    myequalize = integer;
	}

	public boolean isEqualize() {
	    return myequalize;
	}

	public void setMoveEnabled(Boolean bool) {
	    moveEnabled = bool;
	}

	public boolean isMoveEnabled() {
	    return moveEnabled;
	}

	public void setMACDEnabled(Boolean bool) {
	    macdEnabled = bool;
	}

	public boolean isMACDEnabled() {
	    return macdEnabled;
	}

	public void setMACDDeltaEnabled(Boolean bool) {
	    macdDeltaEnabled = bool;
	}

	public void setMACDHistogramDeltaEnabled(Boolean bool) {
	    macdHistogramDeltaEnabled = bool;
	}

	public boolean isRSIDeltaEnabled() {
	    return rsiDeltaEnabled;
	}

	public void setRSIDeltaEnabled(Boolean bool) {
	    rsiDeltaEnabled = bool;
	}

	public boolean isMACDDeltaEnabled() {
	    return macdDeltaEnabled;
	}

	public boolean isMACDHistogramDeltaEnabled() {
	    return macdHistogramDeltaEnabled;
	}

	public void setRSIenabled(Boolean bool) {
	    rsiEnabled = bool;
	}

	public boolean isRSIenabled() {
	    return rsiEnabled;
	}

	public void setGraphEqualize(Boolean integer) {
	    mygraphequalize = integer;
	}

	public boolean isGraphEqualize() {
	    return mygraphequalize;
	}

	public void setGraphEqUnify(Boolean integer) {
	    mygraphequalizeunify = integer;
	}

	public boolean isGraphEqUnify() {
	    return mygraphequalizeunify;
	}

	public int getMACDDeltaDays() {
		return macdDeltaDays;
	}
    
	public void setMACDHistogramDeltaDays(Integer integer) {
		this.macdHistogramDeltaDays = integer;
	}
    
	public int getMACDHistogramDeltaDays() {
		return macdHistogramDeltaDays;
	}
    
	public void setMACDDeltaDays(Integer integer) {
		this.macdDeltaDays = integer;
	}
    
	public int getRSIdiffDays() {
		return rsiDeltaDays;
	}
    
	public void setRSIdiffDays(Integer integer) {
		this.rsiDeltaDays = integer;
	}
    
}
