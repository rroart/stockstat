package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.MLMetricsItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class MLMetricsRowMapper implements RowMapper<MLMetricsItem>{
    @Override
    public MLMetricsItem mapRow(ResultSet rs, int rowNum) throws SQLException {
	MLMetricsItem item = new MLMetricsItem();
 item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
item.setDate(TimeUtil.convertDate(rs.getDate("date")));
item.setMarket(rs.getString("market"));
item.setComponent(rs.getString("component"));
item.setSubcomponent(rs.getString("subcomponent"));
item.setLocalcomponent(rs.getString("localcomponent"));
item.setTrainAccuracy(rs.getDouble("trainaccuracy"));
item.setTestAccuracy(rs.getDouble("testaccuracy"));
item.setLoss(rs.getDouble("loss"));
item.setThreshold(rs.getDouble("threshold"));

return item;   
    }
}
