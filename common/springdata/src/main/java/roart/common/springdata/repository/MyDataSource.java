package roart.common.springdata.repository;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyDataSource {
    @Bean
    public DataSource getDataSource() {
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDD");
        return DataSourceBuilder.create()
          .driverClassName("org.postgresql.Driver")
          .url("jdbc:postgresql://localhost:5432/stockstatdev")
          .username("stockstat")
          .password("password")
          .build();     
    }
    

}
