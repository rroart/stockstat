package roart.common.springdata.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import roart.common.model.ContItem;
import roart.common.util.TimeUtil;

import org.springframework.jdbc.core.RowMapper;

public class ContRowMapper implements RowMapper<ContItem>{
    @Override
    public ContItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContItem item = new ContItem();
        item.setMd5(rs.getString("md5"));
        item.setFilename(rs.getString("filename"));
        item.setDate(TimeUtil.convertDate(rs.getDate("date")));
        return item;
    }
}
