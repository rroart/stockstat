package roart.common.springdata.rowmapper;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.SimDataDTO;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

public class SimDataRowMapper implements RowMapper<SimDataDTO>{
    @Override
    public SimDataDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        SimDataDTO item = new SimDataDTO();
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
