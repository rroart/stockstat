package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.TimingItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class TimingRowMapper implements RowMapper<TimingItem>{
    @Override
    public TimingItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	TimingItem item = new TimingItem();
 item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
    item.setDate(TimeUtil.convertDate(rs.getDate("date")));
    item.setMarket(rs.getString("market"));
item.setMlmarket(rs.getString("mlmarket"));
    item.setAction(rs.getString("action"));
    item.setEvolve(rs.getBoolean("evolve"));
    item.setComponent(rs.getString("component"));
    item.setSubcomponent(rs.getString("subcomponent"));
    item.setParameters(rs.getString("parameters"));
    item.setMytime(rs.getDouble("time"));
item.setScore(rs.getDouble("score"));
    item.setBuy(rs.getBoolean("buy"));
    item.setDescription(rs.getString("description"));
    return item;

    }
}
