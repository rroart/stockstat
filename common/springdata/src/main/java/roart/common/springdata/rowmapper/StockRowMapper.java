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
        Double indexvalue = rs.getDouble("indexvalue");
        Double indexvaluelow = rs.getDouble("indexvaluelow");
        Double indexvaluehigh = rs.getDouble("indexvaluehigh");
        Double indexvalueopen = rs.getDouble("indexvalueopen");
        Double price = rs.getDouble("price");
        Double pricelow = rs.getDouble("pricelow");
        Double pricehigh = rs.getDouble("pricehigh");
        Double priceopen = rs.getDouble("priceopen");
        Long volume = rs.getLong("volume");
        String currency = rs.getString("currency");
        Double period1 = rs.getDouble("period1");
        Double period2 = rs.getDouble("period2");
        Double period3 = rs.getDouble("period3");
        Double period4 = rs.getDouble("period4");
        Double period5 = rs.getDouble("period5");
        Double period6 = rs.getDouble("period6");
        Double period7 = rs.getDouble("period7");
        Double period8 = rs.getDouble("period8");
        Double period9 = rs.getDouble("period9");
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
