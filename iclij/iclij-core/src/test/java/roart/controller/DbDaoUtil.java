package roart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import roart.db.hibernate.DbHibernate;
import roart.db.hibernate.DbHibernateDS;
import roart.common.model.ConfigDTO;
import roart.common.model.StockDTO;
import roart.common.springdata.model.Config;
import roart.common.springdata.model.Stock;
import roart.common.springdata.repository.SpringConfigRepository;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.springdata.repository.ConfigRepository;

@Configuration
@ComponentScan
@Component
@Service
public class DbDaoUtil {

    @Autowired
    SpringConfigRepository repo;

    @Autowired
    ConfigRepository repo2;

    @Autowired
    SpringConfigRepository repo3;

    public List<ConfigDTO> getAll(String market, int type) throws Exception {
        List<StockDTO> list = new ArrayList<>();
        long time0 = System.currentTimeMillis();
        if (type == 0) {
            return DbHibernateDS.instance().getConfigsByMarket(market);
        }
        if (type == 1) {
            return repo2.getAll(market);
        }
        if (type == 2) {
            return StreamSupport.stream(repo3.findAll().spliterator(), false).map(e -> map(e)).toList();
        }
        return null;
    }

    private ConfigDTO map(Config config) {
        ConfigDTO configItem = new ConfigDTO();
        configItem.setAction(config.getAction());
        configItem.setBuy(config.getBuy());
        configItem.setDate(TimeUtil.convertDate(config.getDate()));
        configItem.setId(config.getId());
        configItem.setComponent(config.getComponent());
        configItem.setMarket(config.getMarket());
        configItem.setRecord(TimeUtil.convertDate(config.getRecord()));
        configItem.setParameters(config.getParameters());
        configItem.setScore(config.getScore());
        configItem.setSubcomponent(config.getSubcomponent());
        if (config.getValue() != null) {
            configItem.setValue(JsonUtil.strip(new String(config.getValue())));
        }
        return configItem;
    }

}

