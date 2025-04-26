package roart.model.io;

import org.apache.curator.framework.CuratorFramework;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.DbDao;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;

public class IO {
    private IclijDbDao idbDao;

    private DbDao dbDao;

    private WebFluxUtil webFluxUtil;
    
    private FileSystemDao fileSystemDao;

    private InmemoryFactory inmemoryFactory;
    
    private CommunicationFactory communicationFactory;
    
    private CuratorFramework curatorClient;
 
    public IO(IclijDbDao idbDao, DbDao dbDao, WebFluxUtil webFluxUtil, FileSystemDao fileSystemDao,
            InmemoryFactory inmemoryFactory, CommunicationFactory communicationFactory, CuratorFramework curatorClient) {
        super();
        this.idbDao = idbDao;
        this.dbDao = dbDao;
        this.webFluxUtil = webFluxUtil;
        this.fileSystemDao = fileSystemDao;
        this.inmemoryFactory = inmemoryFactory;
        this.communicationFactory = communicationFactory;
        this.curatorClient = curatorClient;
    }

    public IclijDbDao getIdbDao() {
        return idbDao;
    }

    public void setIdbDao(IclijDbDao idbDao) {
        this.idbDao = idbDao;
    }

    public DbDao getDbDao() {
        return dbDao;
    }

    public void setDbDao(DbDao dbDao) {
        this.dbDao = dbDao;
    }

    public WebFluxUtil getWebFluxUtil() {
        return webFluxUtil;
    }

    public void setWebFluxUtil(WebFluxUtil webFluxUtil) {
        this.webFluxUtil = webFluxUtil;
    }

    public FileSystemDao getFileSystemDao() {
        return fileSystemDao;
    }

    public void setFileSystemDao(FileSystemDao fileSystemDao) {
        this.fileSystemDao = fileSystemDao;
    }

    public InmemoryFactory getInmemoryFactory() {
        return inmemoryFactory;
    }

    public void setInmemoryFactory(InmemoryFactory inmemoryFactory) {
        this.inmemoryFactory = inmemoryFactory;
    }

    public CommunicationFactory getCommunicationFactory() {
        return communicationFactory;
    }

    public void setCommunicationFactory(CommunicationFactory communicationFactory) {
        this.communicationFactory = communicationFactory;
    }

    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public void setCuratorClient(CuratorFramework curatorClient) {
        this.curatorClient = curatorClient;
    }
    
    
}
