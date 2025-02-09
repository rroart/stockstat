package roart.model.io;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.model.MyDataSource;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.DbDao;
import roart.db.dao.IclijDbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;

public class IO {
    private IclijDbDao idbDao;

    private DbDao dbDao;

    private MyDataSource dataSource;
    
    private WebFluxUtil webFluxUtil;
    
    private FileSystemDao fileSystemDao;

    public IO(IclijDbDao idbDao, DbDao dbDao, MyDataSource dataSource, WebFluxUtil webFluxUtil,
            FileSystemDao fileSystemDao) {
        super();
        this.idbDao = idbDao;
        this.dbDao = dbDao;
        this.dataSource = dataSource;
        this.webFluxUtil = webFluxUtil;
        this.fileSystemDao = fileSystemDao;
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

    public MyDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(MyDataSource dataSource) {
        this.dataSource = dataSource;
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
    
    
}
