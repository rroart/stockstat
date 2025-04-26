package roart.db.hibernate;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.AboveBelowDTO;
import roart.common.model.ActionComponentDTO;
import roart.common.model.ConfigDTO;
import roart.common.model.ContDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.MetaDTO;
import roart.common.model.RelationDTO;
import roart.common.model.SimDataDTO;
import roart.common.model.StockDTO;
import roart.common.model.TimingBLDTO;
import roart.common.model.TimingDTO;
import roart.common.pipeline.data.SerialTA;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.model.ActionComponent;
import roart.db.model.HibernateUtil;
import roart.db.model.Meta;
import roart.db.model.Stock;
import roart.db.model.Timing;
import roart.db.model.Memory;
import roart.db.model.AboveBelow;
import roart.db.model.Config;
import roart.db.model.Cont;
import roart.db.model.IncDec;
import roart.db.model.MLMetrics;
import roart.db.model.Relation;
import roart.db.model.SimData;
import roart.db.model.TimingBL;
import roart.db.thread.Queues;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbHibernate {

    private static Logger log = LoggerFactory.getLogger(DbHibernate.class);

    public DbHibernate() {
    }

    public static List<StockDTO> getAll(String market) throws Exception {
        try {
            return Stock.getAll(market).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<StockDTO> getAll2(String market) throws Exception {
        List<StockDTO> stockitems = new ArrayList<>();
        long time0 = System.currentTimeMillis();
        Connection conn = DriverManager.getConnection(System.getProperty("connection.url"));
        PreparedStatement st = conn.prepareStatement("select * from Stock where marketid = ?");
        st.setString(1, market);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String dbid = rs.getString("dbid");
            String marketid = rs.getString("marketid");
            String id = rs.getString("id");
            String isin = rs.getString("isin");
            String name = rs.getString("name");
            Date date = rs.getDate("date");
            Double indexvalue = rs.getDouble("indexvalue");
            Double indexvaluelow = rs.getDouble("indexvaluelow");
            Double indexvaluehigh = rs.getDouble("indexvaluehigh");
            Double indexvalueopen = rs.getDouble("indexvalueopen");
            Double price = rs.getDouble("price");
            Double pricelow = rs.getDouble("pricelow");
            Double pricehigh = rs.getDouble("pricehigh");
            Double priceopen = rs.getDouble("priceopen");
            Long volume = rs.getLong("volume");
            String currency = rs.getString("currency");
            Double period1 = rs.getDouble("period1");
            Double period2 = rs.getDouble("period2");
            Double period3 = rs.getDouble("period3");
            Double period4 = rs.getDouble("period4");
            Double period5 = rs.getDouble("period5");
            Double period6 = rs.getDouble("period6");
            Double period7 = rs.getDouble("period7");
            Double period8 = rs.getDouble("period8");
            Double period9 = rs.getDouble("period9");
            // \([A-Za-z]+\) \([A-Za-z]+\) → \1 \2 = rs.get\1("\2")
            StockDTO stockItem = new StockDTO(dbid, marketid, id, isin, name, date, indexvalue, indexvaluelow, indexvaluehigh, indexvalueopen, price, pricelow, pricehigh, priceopen, volume, currency, period1, period2, period3, period4, period5, period6, period7, period8, period9);
            // stock.get\([A-Z]\) → \,(downcase \1))
            stockitems.add(stockItem);
        }
        conn.close();
        return stockitems;
    }

    public static List<MetaDTO> getAll() throws Exception {
        long time0 = System.currentTimeMillis();
        List<MetaDTO> metas = getMetas();
        log.info("time0 " + (System.currentTimeMillis() - time0));
        return metas;
    }

    public static MetaDTO getMarket(String market) throws Exception {
        List<Meta> metas = Meta.getAll(market);
        if (metas == null || metas.isEmpty() || metas.size() > 1) {
            return null;
        }
        Meta meta = metas.get(0);
        return new MetaDTO(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
    }

    private static AboveBelowDTO map(AboveBelow data) {
        AboveBelowDTO item = new AboveBelowDTO();
        item.setComponents(data.getComponents());
        item.setDate(data.getDate());
        item.setMarket(data.getMarket());
        item.setRecord(data.getRecord());
        item.setScore(data.getScore());
        item.setSubcomponents(data.getSubcomponents());
        return item;
    }

    private static ActionComponentDTO map(ActionComponent ac) {
        ActionComponentDTO item = new ActionComponentDTO();
        item.setAction(ac.getAction());
        item.setBuy(ac.getBuy());
        item.setDbid(ac.getDbid());
        //configItem.setDate(TimeUtil.convertDate(config.getDate()));
        //configItem.setId(config.getId());
        item.setComponent(ac.getComponent());
        item.setMarket(ac.getMarket());
        item.setRecord(ac.getRecord());
        item.setParameters(ac.getParameters());
        //configItem.setScore(config.getScore());
        item.setSubcomponent(ac.getSubcomponent());
        item.setPriority(ac.getPriority());
        //configItem.setValue(JsonUtil.strip(config.getValue()));
        return item;
    }

    private static Stock map(StockDTO item) {
        Stock stock = new Stock();
        stock.setDbid(item.getDbid());
        stock.setMarketid(item.getMarketid());
        stock.setId(item.getId());
        stock.setIsin(item.getIsin());
        stock.setName(item.getName());
        stock.setDate(item.getDate());
        stock.setIndexvalue(item.getIndexvalue());
        stock.setIndexvaluelow(item.getIndexvaluelow());
        stock.setIndexvaluehigh(item.getIndexvaluehigh());
        stock.setIndexvalueopen(item.getIndexvalueopen());
        stock.setPrice(item.getPrice());
        stock.setPricelow(item.getPricelow());
        stock.setPricehigh(item.getPricehigh());
        stock.setPriceopen(item.getPriceopen());
        stock.setVolume(item.getVolume());
        stock.setCurrency(item.getCurrency());
        stock.setPeriod1(item.getPeriod(1));
        stock.setPeriod2(item.getPeriod(2));
        stock.setPeriod3(item.getPeriod(3));
        stock.setPeriod4(item.getPeriod(4));
        stock.setPeriod5(item.getPeriod(5));
        stock.setPeriod6(item.getPeriod(6));
        stock.setPeriod7(item.getPeriod(7));
        stock.setPeriod8(item.getPeriod(8));
        stock.setPeriod9(item.getPeriod(9));
        return stock;
    }

    private static StockDTO map(Stock stock) {
        StockDTO item = new StockDTO();
        item.setDbid(stock.getDbid());
        item.setMarketid(stock.getMarketid());
        item.setId(stock.getId());
        item.setIsin(stock.getIsin());
        item.setName(stock.getName());
        item.setDate(stock.getDate());
        item.setIndexvalue(stock.getIndexvalue());
        item.setIndexvaluelow(stock.getIndexvaluelow());
        item.setIndexvaluehigh(stock.getIndexvaluehigh());
        item.setIndexvalueopen(stock.getIndexvalueopen());
        item.setPrice(stock.getPrice());
        item.setPricelow(stock.getPricelow());
        item.setPricehigh(stock.getPricehigh());
        item.setPriceopen(stock.getPriceopen());
        item.setVolume(stock.getVolume());
        item.setCurrency(stock.getCurrency());
        item.setPeriod(0, stock.getPeriod1());
        item.setPeriod(1, stock.getPeriod2());
        item.setPeriod(2, stock.getPeriod3());
        item.setPeriod(3, stock.getPeriod4());
        item.setPeriod(4, stock.getPeriod5());
        item.setPeriod(5, stock.getPeriod6());
        item.setPeriod(6, stock.getPeriod7());
        item.setPeriod(7, stock.getPeriod8());
        item.setPeriod(8, stock.getPeriod9());
        return item;
    }

    private static StockDTO map2(Stock stock) {
        try {
            return new StockDTO(stock.getDbid(), stock.getMarketid(), stock.getId(), stock.getIsin(), stock.getName(), stock.getDate(), stock.getIndexvalue(), stock.getIndexvaluelow(), stock.getIndexvaluehigh(), stock.getIndexvalueopen(), stock.getPrice(), stock.getPricelow(), stock.getPricehigh(), stock.getPriceopen(), stock.getVolume(), stock.getCurrency(), stock.getPeriod1(), stock.getPeriod2(), stock.getPeriod3(), stock.getPeriod4(), stock.getPeriod5(), stock.getPeriod6(), stock.getPeriod7(), stock.getPeriod8(), stock.getPeriod9());
        } catch (Exception e) {
            return null;
        }
    }

    private static MetaDTO map2(Meta meta) {
        return new MetaDTO(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
    }

    private static MetaDTO map(Meta meta) {
        MetaDTO item = new MetaDTO();
        item.setMarketid(meta.getMarketid());
        item.setPeriod(0, meta.getPeriod1());
        item.setPeriod(1, meta.getPeriod2());
        item.setPeriod(2, meta.getPeriod3());
        item.setPeriod(3, meta.getPeriod4());
        item.setPeriod(4, meta.getPeriod5());
        item.setPeriod(5, meta.getPeriod6());
        item.setPeriod(6, meta.getPeriod7());
        item.setPeriod(7, meta.getPeriod8());
        item.setPeriod(8, meta.getPeriod9());
        item.setPriority(meta.getPriority());
        item.setReset(meta.getReset());
        return item;
    }

    private static Meta map(MetaDTO meta) {
        Meta item = new Meta();
        item.setMarketid(meta.getMarketid());
        item.setPeriod1(meta.getPeriod(0));
        item.setPeriod2(meta.getPeriod(1));
        item.setPeriod3(meta.getPeriod(2));
        item.setPeriod4(meta.getPeriod(3));
        item.setPeriod5(meta.getPeriod(4));
        item.setPeriod6(meta.getPeriod(5));
        item.setPeriod7(meta.getPeriod(6));
        item.setPeriod8(meta.getPeriod(7));
        item.setPeriod9(meta.getPeriod(8));
        item.setPriority(meta.getPriority());
        item.setReset(meta.getReset());
        return item;
    }

    private static ConfigDTO map(Config config) {
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
            configItem.setValue(JsonUtil.strip(new String(config.getValue(), StandardCharsets.UTF_8)));
        }
        return configItem;
    }

    private static Cont map(ContDTO item) {
        Cont cont = new Cont();
        cont.setDate(item.getDate());
        cont.setFilename(item.getFilename());
        cont.setMd5(item.getMd5());
        return cont;
    }

    private static ContDTO map(Cont cont) {
        ContDTO contItem = new ContDTO();
        contItem.setDate(cont.getDate());
        contItem.setFilename(cont.getFilename());
        contItem.setMd5(cont.getMd5());
        return contItem;
    }

    private static IncDecDTO map(IncDec incdec) {
        IncDecDTO incdecItem = new IncDecDTO();
        incdecItem.setComponent(incdec.getComponent());
        incdecItem.setDate(TimeUtil.convertDate(incdec.getDate()));
        incdecItem.setDescription(incdec.getDescription());
        incdecItem.setId(incdec.getId());
        incdecItem.setIncrease(incdec.isIncrease());
        incdecItem.setLocalcomponent(incdec.getLocalcomponent());
        incdecItem.setMarket(incdec.getMarket());
        incdecItem.setName(incdec.getName());
        incdecItem.setParameters(incdec.getParameters());
        incdecItem.setRecord(TimeUtil.convertDate(incdec.getRecord()));
        incdecItem.setScore(incdec.getScore());
        incdecItem.setSubcomponent(incdec.getSubcomponent());;
        return incdecItem;
    }

    private static MemoryDTO map(Memory memory) {
        MemoryDTO memoryItem = new MemoryDTO();
        memoryItem.setAction(memory.getAction());
        memoryItem.setAbovepositives(memory.getAbovepositives());
        memoryItem.setAbovesize(memory.getAbovesize());
        memoryItem.setBelowpositives(memory.getBelowpositives());
        memoryItem.setBelowsize(memory.getBelowsize());
        memoryItem.setCategory(memory.getCategory());
        memoryItem.setComponent(memory.getComponent());
        memoryItem.setConfidence(memory.getConfidence());
        memoryItem.setDate(TimeUtil.convertDate(memory.getDate()));
        memoryItem.setDescription(memory.getDescription());
        memoryItem.setFn(memory.getFn());
        memoryItem.setFnConf(memory.getFnConf());
        memoryItem.setFnProb(memory.getFnProb());
        memoryItem.setFnProbConf(memory.getFnProbConf());
        memoryItem.setFnSize(memory.getFnSize());
        memoryItem.setFp(memory.getFp());
        memoryItem.setFpConf(memory.getFpConf());
        memoryItem.setFpProb(memory.getFpProb());
        memoryItem.setFpProbConf(memory.getFpProbConf());
        memoryItem.setFpSize(memory.getFpSize());
        memoryItem.setFuturedate(TimeUtil.convertDate(memory.getFuturedate()));
        memoryItem.setFuturedays(memory.getFuturedays());
        memoryItem.setInfo(memory.getInfo());
        memoryItem.setLearnConfidence(memory.getLearnConfidence());
        memoryItem.setLocalcomponent(memory.getLocalcomponent());
        memoryItem.setMarket(memory.getMarket());
        memoryItem.setPosition(memory.getPosition());
        memoryItem.setRecord(TimeUtil.convertDate(memory.getRecord()));
        memoryItem.setPositives(memory.getPositives());
        memoryItem.setSize(memory.getSize());
        memoryItem.setSubcomponent(memory.getSubcomponent());
        memoryItem.setTestaccuracy(memory.getTestaccuracy());
        memoryItem.setTestloss(memory.getTestloss());
        memoryItem.setParameters(memory.getParameters());
        memoryItem.setTn(memory.getTn());
        memoryItem.setTnConf(memory.getTnConf());
        memoryItem.setTnProb(memory.getTnProb());
        memoryItem.setTnProbConf(memory.getTnProbConf());
        memoryItem.setTnSize(memory.getTnSize());
        memoryItem.setTp(memory.getTp());
        memoryItem.setTpConf(memory.getTpConf());
        memoryItem.setTpProb(memory.getTpProb());
        memoryItem.setTpProbConf(memory.getTpProbConf());
        memoryItem.setTpSize(memory.getTpSize());
        memoryItem.setType(memory.getType());
        memoryItem.setUsedsec(memory.getUsedsec());
        return memoryItem;
    }

    private static MLMetricsDTO map(MLMetrics mltest) {
        MLMetricsDTO mltestItem = new MLMetricsDTO();
        mltestItem.setDate(TimeUtil.convertDate(mltest.getDate()));
        mltestItem.setComponent(mltest.getComponent());
        mltestItem.setMarket(mltest.getMarket());
        mltestItem.setRecord(TimeUtil.convertDate(mltest.getRecord()));
        mltestItem.setSubcomponent(mltest.getSubcomponent());
        mltestItem.setLocalcomponent(mltest.getLocalcomponent());
        mltestItem.setThreshold(mltest.getThreshold());
        mltestItem.setLoss(mltest.getLoss());
        mltestItem.setTestAccuracy(mltest.getTestAccuracy());
        mltestItem.setTrainAccuracy(mltest.getTrainAccuracy());
        return mltestItem;
    }

    private static RelationDTO map(Relation relation) {
        RelationDTO relationItem = new RelationDTO();
        relationItem.setAltId(relation.getAltId());
        relationItem.setId(relation.getId());
        relationItem.setMarket(relation.getMarket());
        relationItem.setOtherAltId(relation.getOtherAltId());
        relationItem.setOtherId(relation.getOtherId());
        relationItem.setOtherMarket(relation.getOtherMarket());
        relationItem.setRecord(relation.getRecord());
        relationItem.setType(relation.getType());
        relationItem.setValue(relation.getValue());
        return relationItem;
    }

    private static SimDataDTO map(SimData data) {
        SimDataDTO item = new SimDataDTO();
        item.setConfig(new String(data.getConfig(), StandardCharsets.UTF_8));
        item.setDbid(data.getDbid());
        item.setEnddate(data.getEnddate());
        if (data.getFilter() != null) {
            item.setFilter(new String(data.getFilter(), StandardCharsets.UTF_8));
        }
        item.setMarket(data.getMarket());
        item.setRecord(data.getRecord());
        item.setScore(data.getScore());
        item.setStartdate(data.getStartdate());
        return item;
    }

    private static TimingDTO map(Timing timing) {
        TimingDTO timingItem = new TimingDTO();
        timingItem.setAction(timing.getAction());
        timingItem.setBuy(timing.getBuy());
        timingItem.setComponent(timing.getComponent());
        timingItem.setDate(TimeUtil.convertDate(timing.getDate()));
        timingItem.setDescription(timing.getDescription());
        timingItem.setEvolve(timing.isEvolve());
        timingItem.setMarket(timing.getMarket());
        timingItem.setMlmarket(timing.getMlmarket());
        timingItem.setRecord(TimeUtil.convertDate(timing.getRecord()));
        timingItem.setMytime(timing.getTime());
        timingItem.setScore(timing.getScore());
        timingItem.setSubcomponent(timing.getSubcomponent());
        timingItem.setParameters(timing.getParameters());
        return timingItem;
    }

    private static TimingBLDTO map(TimingBL timing) {
        TimingBLDTO timingItem = new TimingBLDTO();
        timingItem.setCount(timing.getCount());
        timingItem.setDbid(timing.getDbid());
        timingItem.setId(timing.getId());
        timingItem.setRecord(timing.getRecord());
        return timingItem;
    }

    public static AboveBelow map(AboveBelowDTO item) {
        AboveBelow data = new AboveBelow();
        data.setComponents(item.getComponents());
        data.setDate(item.getDate());
        data.setMarket(item.getMarket());
        data.setRecord(item.getRecord());
        data.setScore(item.getScore());
        data.setSubcomponents(item.getSubcomponents());
        return data;
    }

    public static ActionComponent map(ActionComponentDTO item) {
        ActionComponent config = new ActionComponent();
        config.setAction(item.getAction());
        config.setBuy(item.getBuy());
        config.setComponent(item.getComponent());
        config.setMarket(item.getMarket());
        config.setRecord(item.getRecord());
        config.setParameters(item.getParameters());
        config.setPriority(item.getPriority());
        config.setSubcomponent(item.getSubcomponent());
        return config;
    }

    public static Config map(ConfigDTO item) {
        Config config = new Config();
        config.setAction(item.getAction());
        config.setBuy(item.getBuy());
        config.setComponent(item.getComponent());
        config.setDate(TimeUtil.convertDate(item.getDate()));
        config.setId(item.getId());
        config.setMarket(item.getMarket());
        config.setParameters(item.getParameters());
        config.setRecord(TimeUtil.convertDate(item.getRecord()));
        config.setScore(item.getScore());
        config.setSubcomponent(item.getSubcomponent());
        if (item.getValue() != null) {
            config.setValue(JsonUtil.strip(item.getValue()).getBytes(StandardCharsets.UTF_8));
        }
        return config;
    }

    public static IncDec map(IncDecDTO item) {
        IncDec incdec = new IncDec();
        incdec.setComponent(item.getComponent());
        incdec.setDate(TimeUtil.convertDate(item.getDate()));
        incdec.setDescription(item.getDescription());
        incdec.setId(item.getId());
        incdec.setIncrease(item.isIncrease());
        incdec.setLocalcomponent(item.getLocalcomponent());
        incdec.setMarket(item.getMarket());
        incdec.setName(item.getName());
        incdec.setParameters(item.getParameters());
        incdec.setRecord(TimeUtil.convertDate(item.getRecord()));
        incdec.setScore(item.getScore());
        incdec.setSubcomponent(item.getSubcomponent());
        return incdec;
    }

    public static MLMetrics map(MLMetricsDTO item) {
        MLMetrics mltest = new MLMetrics();
        mltest.setComponent(item.getComponent());
        mltest.setDate(TimeUtil.convertDate(item.getDate()));
        mltest.setMarket(item.getMarket());
        mltest.setRecord(TimeUtil.convertDate(item.getRecord()));
        mltest.setSubcomponent(item.getSubcomponent());
        mltest.setLocalcomponent(item.getLocalcomponent());
        mltest.setThreshold(item.getThreshold());
        mltest.setLoss(item.getLoss());
        mltest.setTrainAccuracy(item.getTrainAccuracy());
        mltest.setTestAccuracy(item.getTestAccuracy());
        return mltest;
    }

    public static Relation map(RelationDTO item) {
        Relation relation = new Relation();
        relation.setAltId(item.getAltId());
        relation.setId(item.getId());
        relation.setMarket(item.getMarket());
        relation.setOtherAltId(item.getOtherAltId());
        relation.setOtherId(item.getOtherId());
        relation.setOtherMarket(item.getOtherMarket());
        relation.setRecord(item.getRecord());
        relation.setType(item.getType());
        relation.setValue(item.getValue());
        return relation;
    }

    public static Memory map(MemoryDTO item) {
        Memory memory = new Memory();
        memory.setAction(item.getAction());
        memory.setAbovepositives(item.getAbovepositives());
        memory.setAbovesize(item.getAbovesize());
        memory.setBelowpositives(item.getBelowpositives());
        memory.setBelowsize(item.getBelowsize());
        memory.setCategory(item.getCategory());
        memory.setComponent(item.getComponent());
        memory.setConfidence(item.getConfidence());
        memory.setDate(TimeUtil.convertDate(item.getDate()));
        memory.setDescription(item.getDescription());
        memory.setFn(item.getFn());
        memory.setFnConf(item.getFnConf());
        memory.setFnProb(item.getFnProb());
        memory.setFnProbConf(item.getFnProbConf());
        memory.setFnSize(item.getFnSize());
        memory.setFp(item.getFp());
        memory.setFpConf(item.getFpConf());
        memory.setFpProb(item.getFpProb());
        memory.setFpProbConf(item.getFpProbConf());
        memory.setFpSize(item.getFpSize());
        memory.setFuturedate(TimeUtil.convertDate(item.getFuturedate()));
        memory.setFuturedays(item.getFuturedays());
        memory.setInfo(item.getInfo());
        memory.setLearnConfidence(item.getLearnConfidence());
        memory.setLocalcomponent(item.getLocalcomponent());
        memory.setMarket(item.getMarket());
        memory.setPositives(item.getPositives());
        memory.setPosition(item.getPosition());
        memory.setRecord(TimeUtil.convertDate(item.getRecord()));
        memory.setSize(item.getSize());
        memory.setSubcomponent(item.getSubcomponent());
        memory.setTestaccuracy(item.getTestaccuracy());
        memory.setTestloss(item.getTestloss());
        memory.setParameters(item.getParameters());
        memory.setTn(item.getTn());
        memory.setTnConf(item.getTnConf());
        memory.setTnProb(item.getTnProb());
        memory.setTnProbConf(item.getTnProbConf());
        memory.setTnSize(item.getTnSize());
        memory.setTp(item.getTp());
        memory.setTpConf(item.getTpConf());
        memory.setTpProb(item.getTpProb());
        memory.setTpProbConf(item.getTpProbConf());
        memory.setTpSize(item.getTpSize());
        memory.setType(item.getType());
        memory.setUsedsec(item.getUsedsec());
        return memory;
    }

    public static SimData map(SimDataDTO item) {
        SimData data = new SimData();
        data.setConfig(item.getConfig().getBytes(StandardCharsets.UTF_8));
        data.setEnddate(item.getEnddate());
        if (item.getFilter() != null) {
            data.setFilter(item.getFilter().getBytes(StandardCharsets.UTF_8));
        }
        data.setMarket(item.getMarket());
        data.setRecord(item.getRecord());
        data.setScore(item.getScore());
        data.setStartdate(item.getStartdate());
        return data;
    }

    public static TimingBL map(TimingBLDTO item) {
        TimingBL timing = new TimingBL();
        timing.setCount(item.getCount());
        timing.setId(item.getId());
        timing.setRecord(item.getRecord());
        return timing;
    }

    public static Timing map(TimingDTO item) {
        Timing timing = new Timing();
        timing.setAction(item.getAction());
        timing.setBuy(item.getBuy());
        timing.setComponent(item.getComponent());
        timing.setDate(TimeUtil.convertDate(item.getDate()));
        timing.setDescription(item.getDescription());
        timing.setEvolve(item.isEvolve());
        timing.setMarket(item.getMarket());
        timing.setMlmarket(item.getMlmarket());
        timing.setRecord(TimeUtil.convertDate(item.getRecord()));
        timing.setTime(item.getMytime());
        timing.setScore(item.getScore());
        timing.setSubcomponent(item.getSubcomponent());
        timing.setParameters(item.getParameters());
        return timing;
    }

    public static List<String> getMarkets() throws Exception {
        return Stock.getMarkets();
    }

    public static List<MetaDTO> getMetas() throws Exception {
        return Meta.getAll().stream().map(e -> map(e)).toList();
    }

    public static List<MemoryDTO> getMemories() {
        try {
            return Memory.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<MemoryDTO> getMemoriesByMarket(String market) {
        try {
            return Memory.getAll(market).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<MemoryDTO> getMemories(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate) {
        try {
            return Memory.getAll(market, action, component, subcomponent, parameters, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<TimingDTO> getTimings() {
        try {
            return Timing.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<TimingDTO> getTiming(String market, String action, Date startDate, Date endDate) {
        try {
            return Timing.getAll(market, action, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<RelationDTO> getRelations() {
        try {
            return Relation.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<IncDecDTO> getIncDecs() {
        try {
            return IncDec.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<IncDecDTO> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        try {
            return IncDec.getAll(market, startDate, endDate, parameters).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<ConfigDTO> getConfigs(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate) {
        try {
            return Config.getAll(market, action, component, subcomponent, parameters, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<ConfigDTO> getConfigsByMarket(String market) {
        try {
            return Config.getAll(market).stream().map(e -> map(e)).toList();        
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<MLMetricsDTO> getMLMetrics() {
        try {
            return MLMetrics.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<MLMetricsDTO> getMLMetrics(String market, Date startDate, Date endDate) {
        try {
            return MLMetrics.getAll(market, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static Object save(Object object) {
        Object obj2 = null;
        if (object instanceof AboveBelowDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof ActionComponentDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof ConfigDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof ContDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof MemoryDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof MetaDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof MLMetricsDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof RelationDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof SimDataDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof StockDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof TimingDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof TimingBLDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof IncDecDTO obj) {
            obj2 = map(obj);
        }
        if (object instanceof List list) {
            for (Object obj : list) {
                save(obj);
            }
        }
        if (obj2 == null) {
            log.error("Unknown save object");
            return null;
        }
        Queues.queue.add(obj2);
        if (true)         return null;
        if (object instanceof IncDecDTO) {
            //IncDec.save(map((IncDec) object)) ;
        }
        return null;
    }

    public static void deleteById(Object object, String dbid) {
        try {
            if (object instanceof ActionComponentDTO) {
                ActionComponent.delete(Long.valueOf(dbid));
            }
            if (object instanceof TimingBLDTO) {
                // not the @id
                TimingBL.delete(dbid);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);

        }

    }
    public static void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        try {
            if (object instanceof AboveBelowDTO) {
                AboveBelow.delete(market, startDate, endDate);

            }
            if (object instanceof IncDecDTO) {
                IncDec.delete(market, component, subcomponent, startDate, endDate);
            }
            if (object instanceof MemoryDTO) {
                Memory.delete(market, component, subcomponent, startDate, endDate);

            }
            if (object instanceof TimingDTO) {
                Timing.delete(market, action, component, subcomponent, startDate, endDate);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);

        }
        log.error(Constants.EXCEPTION);
        //delete(market, action, component, subcomponent, startDate, endDate);
    }

    public static List<SimDataDTO> getSimData(String market, LocalDate startDate, LocalDate endDate) {
        try {
            return SimData.getAll(market, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
        try {
            return AboveBelow.getAll(market, startDate, endDate).stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<ActionComponentDTO> getAllActionComponent() {
        try {
            return ActionComponent.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<TimingBLDTO> getAllTimingBLDTO() {
        try {
            return TimingBL.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<Date> getDates(String market) {
        try {
            return Stock.getDates(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<ContDTO> getAllCont() {
        try {
            return Cont.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<StockDTO> getAllStocks() {
        try {
            return Stock.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<ConfigDTO> getAllConfigs() {
        try {
            return Config.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<SimDataDTO> getAllSimData() {
        try {
            return SimData.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<AboveBelowDTO> getAllAboveBelow() {
        try {
            return AboveBelow.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public static List<SimDataDTO> getAllSimData(String market) {
        try {
            return SimData.getAll().stream().map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }
}

