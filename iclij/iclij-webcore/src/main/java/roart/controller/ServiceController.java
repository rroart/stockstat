package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
import roart.webcore.util.ServiceUtil;

@CrossOrigin
@RestController
public class ServiceController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;
    
    @Autowired
    private IO io;

    private ControlService instance;

    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService(iclijConfig, io);
        }
        return instance;
    }

    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/" + EurekaConstants.GETMARKETS,
            method = RequestMethod.POST)
    public IclijServiceResult getMarkets(@RequestBody IclijServiceParam param)
            throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        try {
            result.setMarkets(getInstance().getMarkets());
            log.info("Marketsize {}", result.getMarkets().size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            result.setError(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = EurekaConstants.GETTASKS,
            method = RequestMethod.POST)
    public List<String> getTasks(/*@PathVariable String market*/)
            throws Exception {
        return getInstance().getTasks();
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getConfig(/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getConfig(getInstance().getConfig());
    }

    @RequestMapping(value = "/core/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getCoreConfig(/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getConfig(getInstance().getCoreConfig());
    }

    @RequestMapping(value = "/core/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public IclijServiceResult getCoreContent(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return getInstance().getCoreContent(param);
    }

    @RequestMapping(value = "/core/" + EurekaConstants.GETCONTENTGRAPH,
            method = RequestMethod.POST)
    public IclijServiceResult getCoreGraphContent(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return getInstance().getCoreContentGraph(param);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public IclijServiceResult getContent(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContent(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTIMPROVE,
            method = RequestMethod.POST)
    public IclijServiceResult getContentImprove(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentImprove(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTFILTER,
            method = RequestMethod.POST)
    public IclijServiceResult getContentFilter(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentFilter(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTABOVEBELOW,
            method = RequestMethod.POST)
    public IclijServiceResult getContentAboveBelow(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentAboveBelow(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTEVOLVE,
            method = RequestMethod.POST)
    public IclijServiceResult getContentEvolve(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentEvolve(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTDATASET,
            method = RequestMethod.POST)
    public IclijServiceResult getContentDataset(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentDataset(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTCROSSTEST,
            method = RequestMethod.POST)
    public IclijServiceResult getContentCrosstest(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentCrosstest(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTMACHINELEARNING,
            method = RequestMethod.POST)
    public IclijServiceResult getContentMachineLearning(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentMachineLearning(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/" + EurekaConstants.GETVERIFY,
            method = RequestMethod.POST)
    public IclijServiceResult getVerify(@RequestBody IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getVerify(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    @RequestMapping(value = "/findprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getFindProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        return ServiceUtil.getFindProfit(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
        //Map<String, IncDecItem>[] result = new FindProfitAction().getPicks(param.getIclijConfig().getMarket(), false, param.getIclijConfig().getDate(), null, param .getIclijConfig());
       //IclijServiceResult ret = new IclijServiceResult();
       //ret.setError(error);
       //return ret;
    }

    @RequestMapping(value = "/improveabovebelow",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveAboveBelowMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        return ServiceUtil.getImproveAboveBelow(iclijConfig, new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io);
        //Map<String, IncDecItem>[] result = new FindProfitAction().getPicks(param.getIclijConfig().getMarket(), false, param.getIclijConfig().getDate(), null, param .getIclijConfig());
       //IclijServiceResult ret = new IclijServiceResult();
       //ret.setError(error);
       //return ret;
    }

    @RequestMapping(value = "/improveprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        return ServiceUtil.getImproveProfit(new ComponentInput(param.getConfigData(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()), io, iclijConfig);
    }

}
