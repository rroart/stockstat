package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.MLMetricsDTO;
import roart.common.util.TimeUtil;

public class MLMetricsRowMapper implements RowMapper<MLMetricsDTO>{
    @Override
    public MLMetricsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        MLMetricsDTO item = new MLMetricsDTO();
        item.setRecord(TimeUtil.convertDate(rs.getDate("record")));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        item.setMarket(rs.getString("market"));
        item.setComponent(rs.getString("component"));
        item.setSubcomponent(rs.getString("subcomponent"));
        item.setLocalcomponent(rs.getString("localcomponent"));
        item.setTrainAccuracy((Double) rs.getObject("trainaccuracy"));
        item.setTestAccuracy((Double) rs.getObject("testaccuracy"));
        item.setValAccuracy((Double) rs.getObject("valaccuracy"));
        item.setLoss((Double) rs.getObject("loss"));
        item.setThreshold((Double) rs.getObject("threshold"));
        item.setDescription((String) rs.getObject("description"));
        
        return item;   
    }
}
