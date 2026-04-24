package roart.core.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.cache.MyCache;
import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.constants.ServiceConstants;
import roart.common.constants.ServiceConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.core.service.evolution.CoreEvolutionService;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceResult;
import roart.model.io.IO;

import java.util.*;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private CoreControlService getInstance(IclijServiceParam param) {
        return new CoreControlService(io);
    }

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object object, Communication c) {
        QueueElement element = JsonUtil.convert((String) object, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());

        IclijServiceParam param = JsonUtil.convertnostrip(content, IclijServiceParam.class);
        IclijServiceResult r = get(param, c);
        QueueElement elementReply = new QueueElement();
        InmemoryMessage msg = inmemory.send(element.getQueue() + UUID.randomUUID(), r, null);
        elementReply.setMessage(msg);
        log.info("replyto {}", element.getQueue());
        sendReply(element.getQueue(), c, elementReply);
    }

    public IclijServiceResult get(IclijServiceParam param, Communication c) {
        IclijServiceResult result = new IclijServiceResult();
        log.info("Cserv {}", c.getService());
        if (serviceMatch(ServiceConstants.GETCONFIG, c)) {
            try {
                result.setConfigData(iclijConfig.getConfigData());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETMARKETS, c)) {
            try {
                result.setMarkets(getInstance(param).getMarkets());
                log.info("Marketsize {}", result.getMarkets().size());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETMETAS, c)) {
            Map<String, Map<String, Object>> maps = null;
            try {
                result.setMetas(getInstance(param).getMetas());
                log.info("Metasize {}", result.getMetas().size());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            //element.setResult(result);
        }
        if (serviceMatch(ServiceConstants.GETDATES, c)) {
            try {
                getInstance(param).getDates( new IclijConfig(param.getConfigData()), result, param);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETSTOCKS, c)) {
            try {
                result.setStocks(getInstance(param).getStocks(param.getMarket(),  new IclijConfig(param.getConfigData())));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETCONTENT, c)) {
            Map<String, Map<String, Object>> maps = null;
            try {
                long[] mem0 = MemUtil.mem();
                log.info("MEM {}", MemUtil.print(mem0));
                List<String> disableList = param.getConfList();
                if (disableList == null) {
                    disableList = new ArrayList<>();
                }
                getInstance(param).getContent( new IclijConfig(param.getConfigData()), disableList, result, param);
                long[] mem1 = MemUtil.mem();
                long[] memdiff = MemUtil.diff(mem1, mem0);
                log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
                log.info("Cache {}", MyCache.getInstance().toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETCONTENTSTAT, c)) {
            Map<String, Map<String, Object>> maps = null;
            try {
                result.setList(getInstance(param).getContentStat( new IclijConfig(param.getConfigData())));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            //element.setResult(result);
        }
        if (serviceMatch(ServiceConstants.GETCONTENTGRAPH, c)) {
            Map<String, Map<String, Object>> maps = null;
            try {
                result.setList(getInstance(param).getContentGraph( new IclijConfig(param.getConfigData()), param.getGuiSize()));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            //element.setResult(result);
        }
        if (serviceMatch(ServiceConstants.GETCONTENTGRAPH2, c)) {
            try {
                Set<Pair<String,String>> ids = new HashSet<>();
                for (String union : param.getIds()) {
                    String[] idsplit = union.split(",");
                    Pair<String, String> pair = new ImmutablePair(idsplit[0], idsplit[1]);
                    ids.add(pair);
                }
                result.setList(getInstance(param).getContentGraph( new IclijConfig(param.getConfigData()), ids, param.getGuiSize()));
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            //element.setResult(result);
        }
        if (serviceMatch(ServiceConstants.GETEVOLVERECOMMENDER, c)) {
            Map<String, Map<String, Object>> maps = null;
            try {
                IclijConfig aConfig = new IclijConfig(param.getConfigData());
                List<String> disableList = param.getConfList();
                if (disableList == null) {
                    disableList = new ArrayList<>();
                }
                result = new CoreEvolutionService(io).getEvolveRecommender( aConfig, disableList, param);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        return result;
    }
}
