package roart.controller;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.model.MyDataSource;
import roart.common.webflux.WebFluxUtil;
import roart.core.model.impl.DbDataSource;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.db.dao.DbDao;
import roart.model.io.IO;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IoBean {

    @Bean
    public InmemoryFactory getInmemoryFactory() {
        return new InmemoryFactory();
    }
    
    @Bean
    public CommunicationFactory getCommunicationFactory() {
        return new CommunicationFactory();
    }

    @Bean
    public MyDataSource getDataSource(DbDao dbDao, IclijConfig conf) {
        return new DbDataSource(dbDao, conf);
    }
    
    @Bean
    public WebFluxUtil getWebFluxUtil() {
        return new WebFluxUtil();
    }
    
    @Bean
    public FileSystemDao getFileSystemDao(IclijConfig config, CuratorFramework curatorClient) {
        return new FileSystemDao(config, curatorClient);
    }
    
    @Bean
    public IO getIo(IclijDbDao iclijDbDao, DbDao dbDao, MyDataSource dataSource, WebFluxUtil webFluxUtil, FileSystemDao fileSystemDao, InmemoryFactory inmemoryFactory, CommunicationFactory communicationFactory, CuratorFramework curatorClient) {
        return new IO(iclijDbDao, dbDao, dataSource, webFluxUtil, fileSystemDao, inmemoryFactory, communicationFactory, curatorClient);

    }

    @Bean
    public CuratorFramework configCurator(IclijConfig conf) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);        
        String zookeeperConnectionString = conf.getZookeeper();
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        curatorClient.start();
        return curatorClient;
    }

}
