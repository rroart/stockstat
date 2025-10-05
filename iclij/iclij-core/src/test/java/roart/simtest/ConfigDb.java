package roart.simtest;

import org.springframework.context.annotation.Bean;

import roart.controller.TestDbDS;
import roart.db.common.DbDS;
import roart.db.spring.DbSpringDS;

public class ConfigDb {

    @Bean
    private DbDS getDbAccess() {
        return new DbSpringDS();
    }
}
