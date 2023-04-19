package roart.common.springdata.repository;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import roart.common.springdata.model.ActionComponent;
import roart.common.springdata.model.TimingBL;
import java.sql.SQLException;

@Configuration
public class MyDataSource {
    //@Bean
    public DataSource getDataSource() {
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDD");
        return DataSourceBuilder.create()
          .driverClassName("org.postgresql.Driver")
          .url("jdbc:postgresql://localhost:5432/stockstatdev")
          .username("stockstat")
          .password("password")
          .build();     
    }
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    //@Bean
    public ApplicationListener<BeforeSaveEvent> idGenerator() {
        return event -> {
            var entity = event.getEntity();
            if (entity instanceof TimingBL tbl) {
                //((TimingBL) entity).setId(UUID.randomUUID().toString());
                if (tbl.getDbid() == null) {
                    System.out.println("Get the next value from a database sequence and use it as the primary key");
         
                    Long id = jdbcTemplate.query("select nextval('timingbl_seq')",
                            rs -> {
                                if (rs.next()) {
                                    return rs.getLong(1);
                                } else {
                                    System.out.println("sqle");
                                    throw new SQLException("Unable to retrieve value from sequence timingbl_seq.");
                                }
                            });
                    System.out.println("sqlid" + id);
                    tbl.setDbid(id);
                }
            }
            if (entity instanceof ActionComponent tbl) {
                //((TimingBL) entity).setId(UUID.randomUUID().toString());
                if (tbl.getDbid() == null) {
                    System.out.println("Get the next value from a database sequence and use it as the primary key");
         
                    Long id = jdbcTemplate.query("select nextval('actioncomponent_seq')",
                            rs -> {
                                if (rs.next()) {
                                    return rs.getLong(1);
                                } else {
                                    throw new SQLException("Unable to retrieve value from sequence actioncomponent_seq.");
                                }
                            });
                    tbl.setDbid(id);
                }
            }
        };
    }

}
