package roart.controller;

import org.springframework.context.annotation.Bean;

import roart.db.common.DbAccess;

public class ConfigDb {

    @Bean
    private DbAccess getDbAccess() {
        return new TestDbAccess();
    }
    
}
