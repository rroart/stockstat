package roart.model.io.util;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.config.IclijConfig;
import roart.model.io.IO;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.function.Function;

public class IOUtils {
    private static Logger log = LoggerFactory.getLogger(IOUtils.class);

    private IO io;

    private IclijConfig iclijConfig;

    private ObjectMapper mapper;

    public IOUtils(IO io, IclijConfig iclijConfig, ObjectMapper objectMapper) {
        this.io = io;
        this.iclijConfig = iclijConfig;
        this.mapper = objectMapper;
    }

    private <T> T sendCMe(Class<T> myclass, IclijServiceParam param, String service) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications(), iclijConfig.wantRestServices());
        T[] result;// = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONFIG
        Function<String, Boolean> zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = new CommunicationFactory().get(sc.getLeft(), myclass, service, mapper, true, true, true, sc.getRight(), zkRegister, io.getWebFluxUtil());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    private <T> T sendAMe(Class<T> myclass, IclijServiceParam param, String service) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications(), iclijConfig.wantRestServices());
        T[] result;// = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONFIG
        Function<String, Boolean> zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), myclass, service, mapper, true, true, true, sc.getRight(), zkRegister, io.getWebFluxUtil());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    public void send(String service, Object object, ObjectMapper objectMapper) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications(), iclijConfig.wantRestServices());
        Function<String, Boolean> zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, io.getWebFluxUtil());
        c.send(object);
    }

    public void send(String service, QueueElement element, IclijConfig config) {
        element.setQueue(service);
        send(service, element);
    }

    public void send(String service, Object object, IclijConfig config) {
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String id = service + System.currentTimeMillis() + UUID.randomUUID();
        InmemoryMessage message = inmemory.send(id, object);
        send(service, message);
    }

    public void send(String service, Object object) {
        if (object == null) {
            log.error("Empty msg for {}", service);
            return;
        }
        send(service, object, mapper);
    }

    public <T> T[] sendReceive(String service, QueueElement object, ObjectMapper objectMapper) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications(), iclijConfig.wantRestServices());
        Function<String, Boolean> zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), String.class, service, objectMapper, true, true, true, sc.getRight(), zkRegister, io.getWebFluxUtil());
        object.setQueue(c.getReturnService());
        return c.sendReceive(object);
    }

    public <T> T sendReceive(Communication c, IclijServiceParam param) {
        param.setWebpath(c.getReturnService());
        T r = (T) c.sendReceive(param);
        return r;
    }

    public <T> T sendReceive(Class<T> clazz, Object param, String service) {
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig);
        QueueElement element = new QueueElement();
        InmemoryMessage msg = inmemory.send(service + UUID.randomUUID(), param, null);
        element.setMessage(msg);
        String[] array = sendReceive(service, element, null);
        String s = array[0];
        QueueElement q = JsonUtil.convert(s, QueueElement.class);
        String content = inmemory.read(q.getMessage());
        inmemory.delete(q.getMessage());
        System.out.println("content"+ content);
        Class<T> tClazz = clazz;
        return JsonUtil.convertnostrip(content, clazz);
    }

    public <T> T sendReceiveA(Class<T> clazz, Object param, String rest, String service) {
        if (iclijConfig.wantRestServices() || !new ServiceConnectionUtil().useService(service, iclijConfig.getServices())) {
            return io.getWebFluxUtil().sendAMe(clazz, param, rest);
            //return sendAMe(clazz, (IclijServiceParam) param, rest);
        } else {
            return sendReceive(clazz, param, service);
        }
    }

    public <T> T sendReceiveC(Class<T> clazz, Object param, String rest, String service) {
        if (iclijConfig.wantRestServices() || !new ServiceConnectionUtil().useService(service, iclijConfig.getServices())) {
            return io.getWebFluxUtil().sendCMe(clazz, param, rest);
            //return sendCMe(clazz, (IclijServiceParam) param, rest);
        } else {
            return sendReceive(clazz, param, service);
        }
    }

    public <T> T sendReceiveM(Class<T> clazz, Object param, String rest, String service) {
        if (iclijConfig.wantRestServices() || !new ServiceConnectionUtil().useService(service, iclijConfig.getServices())) {
            return io.getWebFluxUtil().sendMMe(clazz, param, rest);
        } else {
            return sendReceive(clazz, param, service);
        }
    }

    public <T> T sendReceiveS(Class<T> clazz, Object param, String service) {
        if (iclijConfig.wantRestServices() || !new ServiceConnectionUtil().useService(service, iclijConfig.getServices())) {
            return io.getWebFluxUtil().sendSMe(clazz, param, service);
        } else {
            return sendReceive(clazz, param, service);
        }
    }
}
