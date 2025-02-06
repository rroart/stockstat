package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import roart.action.ActionThread;
import roart.common.cache.MyCache;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.model.TimingItem;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.DbAccessFactory;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringAccess;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.util.ServiceUtil;
import roart.db.common.DbAccess;

@CrossOrigin
@RestController
public class ServiceController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;

    @Autowired
    private IclijDbDao dbDao;

    @Autowired
    private DbSpringAccess dbSpringAccess;
    
    private ControlService instance;

    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService(iclijConfig);
        }
        return instance;
    }

    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getConfig(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setConfigData(iclijConfig.getConfigData());
            log.debug("configs {}", result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + "i" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getIConfig(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setConfigData(iclijConfig.getConfigData());
            log.debug("configs {}", result.getConfigData());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETVERIFY,
            method = RequestMethod.POST)
    public IclijServiceResult getVerify(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getVerify(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "action/findprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getFindProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getFindProfit(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), true, false, new ArrayList<>(), new HashMap<>()), new ArrayList<>(), dbDao, myConfig);
        //Map<String, IncDecItem>[] result = new FindProfitAction().getPicks(param.getIclijConfig().getMarket(), false, param.getIclijConfig().getDate(), null, param .getIclijConfig());
        //IclijServiceResult ret = new IclijServiceResult();
        //ret.setError(error);
        //return ret;
    }

    @RequestMapping(value = "action/improveabovebelow",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveAboveBelowMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getImproveAboveBelow(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), true, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "action/improvefilter",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveFilterMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getImproveFilter(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), true, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "action/improveprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getImproveProfit(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), true, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "action/simulateinvest",
            method = RequestMethod.POST)
    public IclijServiceResult getSimulateInvest(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        IclijConfig myConfig = new IclijConfig(param.getConfigData());
        return ServiceUtil.getSimulateInvest(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), true, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig, webFluxUtil);
    }

    @RequestMapping(value = "action/simulateinvest/market/{market}",
            method = RequestMethod.POST)
    public IclijServiceResult getSimulateInvestMarket(@PathVariable("market") String market, @RequestBody SimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iclijConfig.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig, webFluxUtil);
    }

    @RequestMapping(value = "action/improvesimulateinvest/market/{market}",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveSimulateInvest(@PathVariable("market") String market, @RequestBody SimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iclijConfig.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.SIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.SIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), dbDao, myConfig);
    }

    @RequestMapping(value = "action/autosimulateinvest/market/{market}",
            method = RequestMethod.POST)
    public IclijServiceResult getAutoSimulateInvestMarket(@PathVariable("market") String market, @RequestBody AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iclijConfig.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getAutoSimulateInvest(myConfig, new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), dbDao);
    }

    @RequestMapping(value = "action/improveautosimulateinvest/market/{market}",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveAutoSimulateInvest(@PathVariable("market") String market, @RequestBody AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iclijConfig.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        //myConfig.getConfigData().unmute();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        //myConfig.getConfigData().mute();
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveAutoSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), dbDao, myConfig);
    }

    @RequestMapping(value = "action/improvefilter/market/{market}/ga/{ga}",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveFilter(@PathVariable("market") String market, @PathVariable Integer ga)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iclijConfig.copy();
        if (ga != null) {
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        return ServiceUtil.getImproveFilter(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "action/improveabovebelow/market/{market}/ga/{ga}",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveAboveBelow(@PathVariable("market") String market, @PathVariable Integer ga)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iclijConfig.copy();
        if (ga != null) {
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        return ServiceUtil.getImproveAboveBelow(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), dbDao, myConfig);
    }

    @RequestMapping(value = "cache/invalidate",
            method = RequestMethod.POST)
    public void cacheinvalidate()
            throws Exception {
        MyCache.getInstance().invalidate();          
    }

    @PostMapping(value = "db/update/start")
    public void dbupdatestart()
            throws Exception {
        ActionThread.setUpdateDb(true);
    }

    @PostMapping(value = "db/update/end")
    public void dbupdateend()
            throws Exception {
        ActionThread.setUpdateDb(false);
    }
    @PostMapping(value = "event/pause")

    public void eventpause()
            throws Exception {
        ActionThread.setPause(true);
    }

    @PostMapping(value = "event/continue")
    public void eventcontinue()
            throws Exception {
        ActionThread.setPause(false);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETTASKS,
            method = RequestMethod.POST)
    public List<String> getTasks()
            throws Exception {
        try {
            return IclijController.taskList;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }
    
    @PostMapping(value = "/" + "copy" + "/{dbin}/{dbout}")
    public void cp(@PathVariable("dbin") String dbIn, @PathVariable("dbout") String dbOut)
            throws Exception {
        DbAccess in = DbAccessFactory.get(dbIn, dbSpringAccess);
        DbAccess out = DbAccessFactory.get(dbOut, dbSpringAccess);
        out.save(in.getAllStocks());
        out.save(in.getAllMetas());
        out.save(in.getAllMemories());
        out.save(in.getAllIncDecs());
        out.save(in.getAllConfigs());
        out.save(in.getAllTimings());
        out.save(in.getAllTimingBL());
        out.save(in.getAllRelations());
        out.save(in.getAllConts());
        out.save(in.getAllMLMetrics());
        out.save(in.getAllSimData());
        out.save(in.getAllActionComponent());
        out.save(in.getAllAboveBelow());
    }

}
