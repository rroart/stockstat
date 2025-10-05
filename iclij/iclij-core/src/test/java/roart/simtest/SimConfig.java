package roart.simtest;

import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import roart.common.model.MyDataSource;
import roart.common.springdata.repository.AboveBelowRepository;
import roart.common.springdata.repository.ActionComponentRepository;
import roart.common.springdata.repository.ConfigRepository;
import roart.common.springdata.repository.ContRepository;
import roart.common.springdata.repository.IncDecRepository;
import roart.common.springdata.repository.MLMetricsRepository;
import roart.common.springdata.repository.MemoryRepository;
import roart.common.springdata.repository.MetaRepository;
import roart.common.springdata.repository.RelationRepository;
import roart.common.springdata.repository.SimDataRepository;
import roart.common.springdata.repository.SpringAboveBelowRepository;
import roart.common.springdata.repository.SpringActionComponentRepository;
import roart.common.springdata.repository.SpringConfigRepository;
import roart.common.springdata.repository.SpringContRepository;
import roart.common.springdata.repository.SpringIncDecRepository;
import roart.common.springdata.repository.SpringMLMetricsRepository;
import roart.common.springdata.repository.SpringMemoryRepository;
import roart.common.springdata.repository.SpringMetaRepository;
import roart.common.springdata.repository.SpringRelationRepository;
import roart.common.springdata.repository.SpringSimDataRepository;
import roart.common.springdata.repository.SpringSimRunDataRepository;
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.SpringTimingBLRepository;
import roart.common.springdata.repository.SpringTimingRepository;
import roart.common.springdata.repository.StockRepository;
import roart.common.springdata.repository.TimingBLRepository;
import roart.common.springdata.repository.TimingRepository;
import roart.core.model.impl.DbDataSource;
import roart.db.dao.CoreDataSource;
import roart.db.dao.DbDao;
import roart.db.spring.DbSpringDS;
import roart.iclij.config.IclijConfig;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

@Configuration
public class SimConfig {

    //@Bean
    public RelationalMappingContext getRelationalMappingContext() {
        return new RelationalMappingContext();
    }
    /*
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate; () {
        return new NamedParameterJdbcTemplate(dataSource());
    }*/
    /*
    @Bean
    public NamedParameterJdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public JdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
*/
    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
          //.driverClassName("org.postgresql.Driver")
          .url(System.getenv("DATASOURCE"))
          //.username("stockstat")
          //.password("password")
          .build();     
    }
  /*  
    //@Bean
    public MyDataSource getMyDataSource2(DbDao dbDao, IclijConfig conf) {
        return new DbDataSource(dbDao, conf);
    }
    */
    @Bean
    public MyDataSource getMyDataSource(IclijConfig iclijConfig, DbSpringDS dbSpringDS) {
        return new CoreDataSource(iclijConfig, dbSpringDS);
    }
    
    @Bean
    public DbDao getDbDao(IclijConfig iclijConfig, MyDataSource dataSource) {
        return new DbDao(iclijConfig, dataSource);
    }
/*
    @Bean
    public SpringAboveBelowRepository springAboveBelowRepository() {
        return mock(SpringAboveBelowRepository.class);
    }

    @Bean
    AboveBelowRepository aboveBelowRepo() { return mock(AboveBelowRepository.class); }

    @Bean
    SpringActionComponentRepository springActionComponentRepo() { return mock(SpringActionComponentRepository.class); }

    @Bean
    ActionComponentRepository actionComponentRepo() { return mock(ActionComponentRepository.class); }

    @Bean
    SpringConfigRepository springConfigRepo() { return mock(SpringConfigRepository.class); }

    @Bean
    ConfigRepository configRepo() { return mock(ConfigRepository.class); }

    @Bean
    SpringContRepository springContRepo() { return mock(SpringContRepository.class); }

    @Bean
    ContRepository contRepo() { return mock(ContRepository.class); }

    @Bean
    SpringStockRepository springStockRepo() { return mock(SpringStockRepository.class); }
    
    @Bean
    StockRepository stockRepo() { return new StockRepository(); }

    @Bean
    SpringMetaRepository springMetaRepo() { return mock(SpringMetaRepository.class); }

    @Bean
    MetaRepository metaRepo() { return new MetaRepository(); }

    @Bean
    SpringIncDecRepository springIncdecRepo() { return mock(SpringIncDecRepository.class); }

    @Bean
    IncDecRepository incdecRepo() { return mock(IncDecRepository.class); }

    @Bean
    SpringMemoryRepository springMemoryRepo() { return mock(SpringMemoryRepository.class); }

    @Bean
    MemoryRepository memoryRepo() { return mock(MemoryRepository.class); }

    @Bean
    SpringMLMetricsRepository springMLMetricsRepo() { return mock(SpringMLMetricsRepository.class); }

    @Bean
    MLMetricsRepository mlmetricsRepo() { return mock(MLMetricsRepository.class); }

    @Bean
    SpringRelationRepository springRelationRepo() { return mock(SpringRelationRepository.class); }

    @Bean
    RelationRepository relationRepo() { return mock(RelationRepository.class); }

    @Bean
    SpringSimDataRepository springSimDataRepo() { return mock(SpringSimDataRepository.class); }

    @Bean
    SpringSimRunDataRepository springSimRunDataRepo() { return mock(SpringSimRunDataRepository.class); }

    @Bean
    SimDataRepository simDataRepo() { return mock(SimDataRepository.class); }

    @Bean
    SpringTimingRepository springTimingRepo() { return mock(SpringTimingRepository.class); }

    @Bean
    TimingRepository timingRepo() { return mock(TimingRepository.class); }

    @Bean
    SpringTimingBLRepository springTimingBLRepo() { return mock(SpringTimingBLRepository.class); }

    @Bean
    TimingBLRepository timingBLRepo() { return mock(TimingBLRepository.class); }
*/
}
