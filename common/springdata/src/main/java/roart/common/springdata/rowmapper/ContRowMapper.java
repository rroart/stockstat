package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.ContDTO;
import roart.common.util.TimeUtil;

public class ContRowMapper implements RowMapper<ContDTO>{
    @Override
    public ContDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContDTO item = new ContDTO();
        item.setMd5(rs.getString("md5"));
        item.setFilename(rs.getString("filename"));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        return item;
    }
}
