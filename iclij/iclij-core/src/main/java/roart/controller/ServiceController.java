package roart.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import roart.action.FindProfitAction;
import roart.action.ImproveProfitAction;
import roart.action.MainAction;
import roart.action.UpdateDBAction;
import roart.config.ConfigConstants;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.service.IclijServiceParam;
import roart.service.IclijServiceResult;
import roart.service.ServiceParam;
import roart.util.Constants;
import roart.util.EurekaConstants;
import roart.util.ServiceUtil;

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

    @RequestMapping(value = "/" + EurekaConstants.GETCONTENT,
            method = RequestMethod.POST)
    public IclijServiceResult getContent(/*@PathVariable String market*/)
            throws Exception {
        return ServiceUtil.getContent();
    }

    @RequestMapping(value = "/" + EurekaConstants.GETVERIFY,
            method = RequestMethod.POST)
    public IclijServiceResult getVerify(@RequestBody IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getVerify(param.getVerifyConfig());
    }

    @RequestMapping(value = "/recommender/{market}",
            method = RequestMethod.GET)
    public void getRecommender(@PathVariable String market)
            throws Exception {
        ServiceUtil.doRecommender(market, 0, null, true, new ArrayList<>(), true);
    }

    @RequestMapping(value = "/predictor/{market}",
            method = RequestMethod.GET)
    public void getPredict(@PathVariable String market)
            throws Exception {
        ServiceUtil.doPredict(market, 0, null, true, true);
    }

    @RequestMapping(value = "/mlmacd/{market}",
            method = RequestMethod.GET)
    public void getMLMACD(@PathVariable String market)
            throws Exception {
        ServiceUtil.doMLMACD(market, 0, null, true, true);
    }

    @RequestMapping(value = "/mlindicator/{market}",
            method = RequestMethod.GET)
    public void getMLIndicator(@PathVariable String market)
            throws Exception {
        ServiceUtil.doMLIndicator(market, 0, null, true, true);
    }

    @RequestMapping(value = "/findprofit",
            method = RequestMethod.GET)
    public void getFindProfit()
            throws Exception {
        //MainAction.goals.add(new FindProfitAction());
        new FindProfitAction().goal(null);
    }

    @RequestMapping(value = "/improveprofit",
            method = RequestMethod.GET)
    public void getImproveProfit()
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        new ImproveProfitAction().goal(null);
    }


    @RequestMapping(value = "/updatedb",
            method = RequestMethod.GET)
    public void getUpdateDb()
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        new UpdateDBAction().goal(null);
    }

}
