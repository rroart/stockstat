package roart.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import roart.config.ConfigConstants;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.Constants;
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

    @RequestMapping(value = "/recommender/{market}",
            method = RequestMethod.GET)
    public void getRecommender(@PathVariable String market)
            throws Exception {
        ServiceUtil.doRecommender(market, 0, null);
    }

    @RequestMapping(value = "/predictor/{market}",
            method = RequestMethod.GET)
    public void getPredict(@PathVariable String market)
            throws Exception {
        ServiceUtil.doPredict(market, 0, null);
    }

    @RequestMapping(value = "/mlmacd/{market}",
            method = RequestMethod.GET)
    public void getMLMACD(@PathVariable String market)
            throws Exception {
        ServiceUtil.doMLMACD(market, 0, null);
    }

    @RequestMapping(value = "/mlindicator/{market}",
            method = RequestMethod.GET)
    public void getMLIndicator(@PathVariable String market)
            throws Exception {
        ServiceUtil.doMLIndicator(market, 0, null);
    }

}
