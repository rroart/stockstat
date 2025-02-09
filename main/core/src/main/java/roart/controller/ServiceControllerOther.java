package roart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.service.ServiceParam;
import roart.db.dao.DbDao;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceResult;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;
    
    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, DbDao dao, FileSystemDao fileSystemDao) {
        super(myservices, services, communications, replyclass, iclijConfig, null, fileSystemDao);
    }

    public void get(Object param, Communication c) { 
        IclijServiceResult r = null;
        log.info("Cserv {}", c.getService());
        if (serviceMatch(EurekaConstants.GETCONFIG, c)) {
            r = new IclijServiceResult();
            try {
                r.setConfigData(iclijConfig.getConfigData());
                log.info("configs {}", r.getConfigData());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                r.setError(e.getMessage());
            }
        }
        if (param instanceof ServiceParam) {
            sendReply(((ServiceParam) param).getWebpath(), c, r);
        }
    }
}
