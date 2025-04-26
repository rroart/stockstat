package roart.controller;

import org.springframework.context.annotation.Bean;

import roart.db.common.DbDS;

public class ConfigDb {

    @Bean
    private DbDS getDbAccess() {
        return new TestDbDS();
    }
    
}
