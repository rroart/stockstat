package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.ConfigItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class ConfigRowMapper implements RowMapper<ConfigItem>{
    @Override
    public ConfigItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	ConfigItem item = new ConfigItem();
item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
    item.setDate(TimeUtil.convertDate(rs.getDate("date")));
    item.setMarket(rs.getString("market"));
item.setComponent(rs.getString("component"));
item.setSubcomponent(rs.getString("subcomponent"));
item.setParameters(rs.getString("parameters"));
    item.setAction(rs.getString("action"));
    item.setId(rs.getString("id"));
    item.setValue(rs.getString("value"));
    item.setScore(rs.getDouble("score"));
item.setBuy(rs.getBoolean("buy"));
return item;    

    }
}
