package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.MetaItem;

public class MetaRowMapper implements RowMapper<MetaItem>{
    @Override
    public MetaItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	     String marketid = rs.getString("marketid");
     String period1 = rs.getString("period1");
     String period2 = rs.getString("period2");
     String period3 = rs.getString("period3");
     String period4 = rs.getString("period4");
     String period5 = rs.getString("period5");
     String period6 = rs.getString("period6");
     String period7 = rs.getString("period7");
     String period8 = rs.getString("period9");
     String period9 = rs.getString("period9");
     String priority = rs.getString("priority");
     String reset = rs.getString("reset");
     Boolean lhc = rs.getBoolean("lhc");
     return new MetaItem(marketid, period1, period2, period3, period4, period5, period6, period7, period8, period9, priority, reset, lhc);

    }
}
