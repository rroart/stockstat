package roart.common.springdata.rowmapper;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.ConfigDTO;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

public class ConfigRowMapper implements RowMapper<ConfigDTO>{
    @Override
    public ConfigDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ConfigDTO item = new ConfigDTO();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        item.setMarket(rs.getString("market"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setParameters(rs.getString("parameters"));
        item.setAction(rs.getString("action"));
        item.setId(rs.getString("id"));
        if (rs.getBytes("value") != null) {
            item.setValue(JsonUtil.strip(new String(rs.getBytes("value"), StandardCharsets.UTF_8)));
        }
        item.setScore((Double) rs.getObject("score"));
        item.setBuy((Boolean) rs.getObject("buy"));
        return item;    

    }
}
