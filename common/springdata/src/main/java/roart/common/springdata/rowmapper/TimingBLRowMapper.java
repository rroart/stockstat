package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.TimingBLDTO;
import roart.common.util.TimeUtil;

public class TimingBLRowMapper implements RowMapper<TimingBLDTO>{
    @Override
    public TimingBLDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TimingBLDTO item = new TimingBLDTO();
        item.setDbid((Long) rs.getObject("dbid"));
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setId(rs.getString("id"));
        item.setCount((Integer) rs.getObject("count"));
        return item;
    }
}
