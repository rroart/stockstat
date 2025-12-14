package roart.evolvetest;

import org.springframework.context.annotation.Bean;

import roart.db.common.DbDS;
import roart.db.spring.DbSpringDS;

public class ConfigDb {

    @Bean
    private DbDS getDbAccess() {
        return new DbSpringDS();
    }
}
