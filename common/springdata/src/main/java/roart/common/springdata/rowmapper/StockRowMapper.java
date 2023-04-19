package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.StockItem;

public class StockRowMapper implements RowMapper<StockItem> {

    @Override
    public StockItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        String dbid = rs.getString("dbid");
        String marketid = rs.getString("marketid");
        String id = rs.getString("id");
        String isin = rs.getString("isin");
        String name = rs.getString("name");
        Date date = rs.getDate("date");
        Double indexvalue = (Double) rs.getObject("indexvalue");
        Double indexvaluelow = (Double) rs.getObject("indexvaluelow");
        Double indexvaluehigh = (Double) rs.getObject("indexvaluehigh");
        Double indexvalueopen = (Double) rs.getObject("indexvalueopen");
        Double price = (Double) rs.getObject("price");
        Double pricelow = (Double) rs.getObject("pricelow");
        Double pricehigh = (Double) rs.getObject("pricehigh");
        Double priceopen = (Double) rs.getObject("priceopen");
        Long volume = (Long) rs.getObject("volume");
        String currency = rs.getString("currency");
        Double period1 = (Double) rs.getObject("period1");
        Double period2 = (Double) rs.getObject("period2");
        Double period3 = (Double) rs.getObject("period3");
        Double period4 = (Double) rs.getObject("period4");
        Double period5 = (Double) rs.getObject("period5");
        Double period6 = (Double) rs.getObject("period6");
        Double period7 = (Double) rs.getObject("period7");
        Double period8 = (Double) rs.getObject("period8");
        Double period9 = (Double) rs.getObject("period9");
        // \([A-Za-z]+\) \([A-Za-z]+\) → \1 \2 = rs.get\1("\2")
        StockItem stockItem;
        try {
            return new StockItem(dbid, marketid, id, isin, name, date, indexvalue, indexvaluelow, indexvaluehigh, indexvalueopen, price, pricelow, pricehigh, priceopen, volume, currency, period1, period2, period3, period4, period5, period6, period7, period8, period9);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
