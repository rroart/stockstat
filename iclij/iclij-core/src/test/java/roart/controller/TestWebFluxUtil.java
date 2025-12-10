package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.model.MyDataSource;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.common.webflux.WebFluxUtil;
import roart.core.service.CoreControlService;
import roart.core.service.evolution.CoreEvolutionService;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.machinelearning.service.MachineLearningControlService;
import roart.machinelearning.service.evolution.MachineLearningEvolutionService;
import roart.filesystem.FileSystemDao;
import static org.mockito.Mockito.*;
import roart.model.io.IO;

public class TestWebFluxUtil extends WebFluxUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private IclijConfig conf = null;
    
    private MyDataSource dataSource;
    
    private IO io;
    
    public TestWebFluxUtil(IclijConfig conf, MyDataSource dataSource) {
        super();
        this.conf = conf;
        this.dataSource = dataSource;
    }

    public IO getIo() {
        return io;
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public <T> T sendMMe(Class<T> clazz, Object param, String path) {
        log.info("Calling {}", path);
        if (EurekaConstants.GETCONTENT.equals(path)) {
            String json = JsonUtil.convert(param, mapper); // TODO mapper
            Object myparam = JsonUtil.convertnostrip(json, param.getClass(), mapper); // TODO mapper
            IclijServiceParam origparam = (IclijServiceParam) myparam;
            IclijServiceResult result = getContentM(origparam );
            json = JsonUtil.convert(result, mapper);
            myparam = JsonUtil.convertnostrip(json, clazz, mapper);
            //json = JsonUtil.convert(param);
            //T myparam = JsonUtil.convertnostrip(json, clazz);
            return (T) myparam;
        }
        if (EurekaConstants.GETEVOLVENN.equals(path)) {
            IclijServiceParam origparam = JsonUtil.convertAndBack((IclijServiceParam) param, mapper);
            IclijServiceResult result = getTestML(origparam );
            result = JsonUtil.convertAndBack(result, mapper);
            return (T) result;
        }
        return null;
    }

    public <T> T sendCMe(Class<T> clazz, Object param, String path) {
        log.info("Calling {}", path);
        //
        if (EurekaConstants.GETDATES.equals(path)) {
            return (T) getDates((IclijServiceParam) param);
        }
        if (EurekaConstants.GETMETAS.equals(path)) {
            return (T) getMetas((IclijServiceParam) param);
        }
        if (EurekaConstants.GETSTOCKS.equals(path)) {
            return (T) getStocks((IclijServiceParam) param);
        }
        if (EurekaConstants.GETCONTENT.equals(path)) {
            //String n = null;
            //if (n.isEmpty()) return null;
            return (T) getContentC((IclijServiceParam) param);
        }
        if (EurekaConstants.GETEVOLVERECOMMENDER.equals(path)) {
            IclijServiceParam origparam = JsonUtil.convertAndBack((IclijServiceParam) param, mapper);
            IclijServiceResult result = getEvolveRecommender(origparam );
            result = JsonUtil.convertAndBack(result, mapper);
            return (T) result;          
        }
        return null;
    }

    public <T> T sendMeInner(Class<T> myclass, Object param, String url, ObjectMapper objectMapper) {
        log.info("Calling {}", url);
        if (url.contains("getconfig")) {
            IclijServiceResult result = new IclijServiceResult();
            result.setConfigData(conf.getConfigData());
            System.out.println("strstrr3 " + conf.getAbnormalChange() );
    
            return (T) result;    
        }
        return null;
    }

    private IclijServiceResult getContentM(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            //result = getContentM( disableList, param);
            result = new MachineLearningControlService(io).getContent(disableList, param);
            if (!param.isWantMaps()) {
                result.setMaps(null);
            }
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
                //log.info("Length {}", JsonUtil.convert(maps).length());
            }
            //System.out.println(VM.current().details());
            //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    private IclijServiceResult getTestML(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        try {
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            Set<String> ids = param.getIds();
            String ml = ids.iterator().next();
            FileSystemDao fileSystemDao = mock(FileSystemDao.class);
            //when(fileSystemDao.writeFile(anyString(), anyString(), anyString(), anyString())).thenReturn("dummy.txt");
            //when(fileSystemDao.writeFile(any(), any(), any(), any())).thenReturn("dummy.txt");
            doReturn("dummy.txt").when(fileSystemDao).writeFile(any(), any(), any(), any());
            //doReturn("dummy.txt").when(fileSystemDao).writeFile(anyString(), anyString(), anyString(), anyString());
            result = new MachineLearningEvolutionService(io).getEvolveML( disableList, ml, param);
            if (!param.isWantMaps()) {
                result.setMaps(null);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }
    
    private IclijServiceResult getContentC(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Map<String, Object>> maps = null;
        if (param.isWantMaps()) {
            maps = new HashMap<>();
        }
        try {
            long[] mem0 = MemUtil.mem();
            log.info("MEM {}", MemUtil.print(mem0));
            List<String> disableList = param.getConfList();
            if (disableList == null) {
                disableList = new ArrayList<>();
            }
            //getContentC( new IclijConfig(param.getConfigData()), disableList, result);
            new CoreControlService(io).getContent( new IclijConfig(param.getConfigData()), disableList, result, param);
    
            long[] mem1 = MemUtil.mem();
            long[] memdiff = MemUtil.diff(mem1, mem0);
            log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
            if (maps != null) {
                //log.info("Length {}", JsonUtil.convert(maps).length());
            }
            //System.out.println(VM.current().details());
            //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    private IclijServiceResult getDates(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        try {
            System.out.println("Conf use " + param.getConfigData());
            new CoreControlService(io).getDates( new IclijConfig(param.getConfigData()), result, param);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    private IclijServiceResult getMetas(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setMetas(new CoreControlService(io).getMetas());
            log.info("Metasize {}", result.getMetas().size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }
    
    public IclijServiceResult getStocks(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setStocks(new CoreControlService(io).getStocks(param.getMarket(),  new IclijConfig(param.getConfigData())));
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }
    private IclijServiceResult getEvolveRecommender(IclijServiceParam param) {
        IclijServiceResult result = new IclijServiceResult();
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
        return result;
    }
}
