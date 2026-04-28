package roart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.communication.model.Communication;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.constants.ServiceConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.ml.common.MLClassifyDS;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyDS;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.spark.MLClassifySparkLORModel;
import roart.ml.spark.MLClassifySparkLSVCModel;
import roart.ml.spark.MLClassifySparkMLPCModel;
import roart.ml.spark.MLClassifySparkOVRModel;
import roart.model.io.IO;
import roart.spark.MLClassifySparkDS;

import java.util.UUID;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object object, Communication c) {
        QueueElement element = JsonUtil.convert((String) object, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());

        LearnTestClassifyDS param = JsonUtil.convertnostrip(content, LearnTestClassifyDS.class);
        LearnTestClassifyResult r = get(param, c);
        QueueElement elementReply = new QueueElement();
        InmemoryMessage msg = inmemory.send(element.getQueue() + UUID.randomUUID(), r, null);
        elementReply.setMessage(msg);
        log.info("replyto {}", element.getQueue());
        sendReply(element.getQueue(), c, elementReply);
    }

    public LearnTestClassifyResult get(LearnTestClassifyDS param, Communication c) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        log.info("Cserv {}", c.getService());
        if (serviceMatch(ServiceConstants.CLEAN, c)) {
            try {
                MLClassifyDS access = new MLClassifySparkDS(iclijConfig);
                access.clean();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                //result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.LEARNTESTCLASSIFY, c)) {
            try {
                MLClassifyModel model = getModel(param.modelid);
                MLClassifyDS access = new MLClassifySparkDS(iclijConfig);
                result = access.learntestclassify(param.nnconfigs, null, param.learnTestMap, model, param.size, param.outcomes, param.classifyMap, param.shortMap, param.path, param.filename, param.neuralnetcommand, param.mlmeta, param.classify);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                //result.setError(e.getMessage());
            }
        }
        return result;
    }

    // duplicated
    private MLClassifyModel getModel(int modelid) {
        switch (modelid) {
            case MLConstants.LOGISTICREGRESSION:
                return new MLClassifySparkLORModel(iclijConfig);
            case MLConstants.LINEARSUPPORTVECTORCLASSIFIER:
                return new MLClassifySparkLSVCModel(iclijConfig);
            case MLConstants.MULTILAYERPERCEPTRONCLASSIFIER:
                return new MLClassifySparkMLPCModel(iclijConfig);
            case MLConstants.ONEVSREST:
                return new MLClassifySparkOVRModel(iclijConfig);
        }
        return null;
    }

}
