package roart.model;

import roart.db.DbDao;

public class MetaItem {

	private String marketid;
	private String[] period = new String[6];

	public MetaItem(Meta meta) {
		this.marketid = meta.getMarketid();
		this.period[0] = meta.getPeriod1();
		this.period[1] = meta.getPeriod2();
		this.period[2] = meta.getPeriod3();
		this.period[3] = meta.getPeriod4();
		this.period[4] = meta.getPeriod5();
		this.period[5] = meta.getPeriod6();
	}

	public static MetaItem getById(String market) throws Exception {
		return DbDao.instance().getMarket(market);
	}
	public String getperiod(int i) {
		return period[i];
	}

}
