package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import roart.common.constants.EurekaConstants;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
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
    
    @RequestMapping(value = "/" + EurekaConstants.GETVERIFY,
            method = RequestMethod.POST)
    public IclijServiceResult getVerify(@RequestBody IclijServiceParam param)
            throws Exception {
        return ServiceUtil.getVerify(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

    @RequestMapping(value = "action/findprofit",
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

    @RequestMapping(value = "action/improveprofit",
            method = RequestMethod.POST)
    public IclijServiceResult getImproveProfitMarket(@RequestBody IclijServiceParam param)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        return ServiceUtil.getImproveProfit(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
    }

}
