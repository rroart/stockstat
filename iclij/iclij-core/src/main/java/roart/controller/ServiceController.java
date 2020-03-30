package roart.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MainAction;
import roart.action.UpdateDBAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.service.ServiceParam;
import roart.component.model.ComponentInput;
import roart.config.IclijXMLConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.PredictorService;
import roart.service.RecommenderService;
import roart.util.ServiceUtil;

@CrossOrigin
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ControlService instance;

    private ControlService getInstance() {
        if (instance == null) {
            instance = new ControlService();
        }
        return instance;
    }

    @GetMapping(path = "/")
    public ResponseEntity healthCheck() throws Exception {
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @RequestMapping(value = "/" + EurekaConstants.GETCONFIG,
            method = RequestMethod.POST)
    public IclijServiceResult getConfig(/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getConfig();
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public IclijServiceResult getContent(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContent(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTIMPROVE,
            method = RequestMethod.POST)
    public IclijServiceResult getContentImprove(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentImprove(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTFILTER,
            method = RequestMethod.POST)
    public IclijServiceResult getContentFilter(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentFilter(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTEVOLVE,
            method = RequestMethod.POST)
    public IclijServiceResult getContentEvolve(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentEvolve(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTDATASET,
            method = RequestMethod.POST)
    public IclijServiceResult getContentDataset(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentDataset(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTCROSSTEST,
            method = RequestMethod.POST)
    public IclijServiceResult getContentCrosstest(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentCrosstest(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENTMACHINELEARNING,
            method = RequestMethod.POST)
    public IclijServiceResult getContentMachineLearning(@RequestBody IclijServiceParam param/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContentMachineLearning(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/" + EurekaConstants.GETVERIFY,
            method = RequestMethod.POST)
    public IclijServiceResult getVerify(@RequestBody IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getVerify(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/recommender/{market}",
            method = RequestMethod.GET)
    public void getRecommender(@PathVariable String market)
            throws Exception {
        new RecommenderService().doRecommender(new ComponentInput(null, null, market, LocalDate.now(), 0, true, true, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/predictor/{market}",
            method = RequestMethod.GET)
    public void getPredict(@PathVariable String market)
            throws Exception {
        new PredictorService().doPredict(new ComponentInput(null, null, market, null, null, true, true, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/mlmacd/{market}",
            method = RequestMethod.GET)
    public void getMLMACD(@PathVariable String market)
            throws Exception {
        new MLService().doMLMACD(new ComponentInput(null, null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/mlindicator/{market}",
            method = RequestMethod.GET)
    public void getMLIndicator(@PathVariable String market)
            throws Exception {
        new MLService().doMLIndicator(new ComponentInput(null, null, market, null, null, true, true, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/findprofit",
            method = RequestMethod.GET)
    public void getFindProfit()
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        new FindProfitAction().getMarkets(null, new ComponentInput(IclijXMLConfig.getConfigInstance(), null, null, null, 0, true, false, new ArrayList<>(), new HashMap<>()), null, null);
    }

    @RequestMapping(value = "/improveprofit",
            method = RequestMethod.GET)
    public void getImproveProfit()
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //new ImproveProfitAction().goal(null, null);
        new ImproveProfitAction().getMarkets(null, new ComponentInput(IclijXMLConfig.getConfigInstance(), null, null, null, 0, true, false, new ArrayList<>(), new HashMap<>()), null, null);
    }

    @RequestMapping(value = "/findprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getFindProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        return ServiceUtil.getFindProfit(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
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
        return ServiceUtil.getImproveProfit(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/improvefilter",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveFilterMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        return ServiceUtil.getImproveFilter(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "/updatedb",
            method = RequestMethod.GET)
    public void getUpdateDb()
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        new UpdateDBAction().goal(null, null, null);
    }

}
