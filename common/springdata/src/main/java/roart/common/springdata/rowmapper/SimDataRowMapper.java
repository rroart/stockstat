package roart.common.springdata.rowmapper;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.SimDataItem;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class SimDataRowMapper implements RowMapper<SimDataItem>{
    @Override
    public SimDataItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimDataItem item = new SimDataItem();
        item.setDbid((Long) rs.getObject("dbid"));
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setMarket(rs.getString("market"));
        item.setStartdate(TimeUtil.convertDate(rs.getDate("startdate")));
        item.setEnddate(TimeUtil.convertDate(rs.getDate("enddate")));
        item.setScore((Double) rs.getObject("score"));
        if (rs.getBytes("config") != null) {
            item.setConfig(JsonUtil.strip(new String(rs.getBytes("config"), StandardCharsets.UTF_8)));
        }
        if (rs.getBytes("filter") != null) {
            item.setFilter(JsonUtil.strip(new String(rs.getBytes("filter"), StandardCharsets.UTF_8)));
        }
        return item;

    }
}
