package roart.db.spring;

import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.model.AboveBelowItem;
import roart.common.model.ActionComponentItem;
import roart.common.model.ConfigItem;
import roart.common.model.ContItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.MetaItem;
import roart.common.model.RelationItem;
import roart.common.model.SimDataItem;
import roart.common.model.StockItem;
import roart.common.model.TimingBLItem;
import roart.common.model.TimingItem;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.common.springdata.model.AboveBelow;
import roart.common.springdata.model.ActionComponent;
import roart.common.springdata.model.Config;
import roart.common.springdata.model.Cont;
import roart.common.springdata.model.IncDec;
import roart.common.springdata.model.MLMetrics;
import roart.common.springdata.model.Memory;
import roart.common.springdata.model.Meta;
import roart.common.springdata.model.SimData;
import roart.common.springdata.model.Stock;
import roart.common.springdata.model.Timing;
import roart.common.springdata.model.TimingBL;
import roart.common.springdata.model.Relation;
import roart.common.springdata.repository.AboveBelowRepository;
import roart.common.springdata.repository.ActionComponentRepository;
import roart.common.springdata.repository.ConfigRepository;
import roart.common.springdata.repository.ContRepository;
import roart.common.springdata.repository.SpringSimDataRepository;
import roart.common.springdata.repository.IncDecRepository;
import roart.common.springdata.repository.MLMetricsRepository;
import roart.common.springdata.repository.MemoryRepository;
import roart.common.springdata.repository.MetaRepository;
import roart.common.springdata.repository.RelationRepository;
import roart.common.springdata.repository.TimingRepository;
import roart.common.springdata.repository.TimingBLRepository;
import roart.common.springdata.repository.SpringAboveBelowRepository;
import roart.common.springdata.repository.SpringActionComponentRepository;
import roart.common.springdata.repository.SpringConfigRepository;
import roart.common.springdata.repository.SpringContRepository;
import roart.common.springdata.repository.SpringIncDecRepository;
import roart.common.springdata.repository.SpringMLMetricsRepository;
import roart.common.springdata.repository.SpringMemoryRepository;
import roart.common.springdata.repository.SpringMetaRepository;
import roart.common.springdata.repository.SpringRelationRepository;
import roart.common.springdata.repository.SpringTimingRepository;
import roart.common.springdata.repository.SpringTimingBLRepository;
import roart.common.springdata.repository.SpringStockRepository;
import roart.common.springdata.repository.SimDataRepository;
import roart.common.springdata.repository.StockRepository;
import roart.pipeline.common.Calculatable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbSpring {

    private static Logger log = LoggerFactory.getLogger(DbSpring.class);

    @Autowired
    SpringAboveBelowRepository springAboveBelowRepo;

    @Autowired
    AboveBelowRepository aboveBelowRepo;

    @Autowired
    SpringActionComponentRepository springActionComponentRepo;

    @Autowired
    ActionComponentRepository actionComponentRepo;

    @Autowired
    SpringConfigRepository springConfigRepo;

    @Autowired
    ConfigRepository configRepo;

    @Autowired
    SpringContRepository springContRepo;

    @Autowired
    ContRepository contRepo;

    @Autowired
    SpringStockRepository springStockRepo;

    @Autowired
    StockRepository stockRepo;

    @Autowired
    SpringMetaRepository springMetaRepo;

    @Autowired
    MetaRepository metaRepo;

    @Autowired
    SpringIncDecRepository springIncdecRepo;

    @Autowired
    IncDecRepository incdecRepo;

    @Autowired
    SpringMemoryRepository springMemoryRepo;

    @Autowired
    MemoryRepository memoryRepo;

    @Autowired
    SpringMLMetricsRepository springMLMetricsRepo;

    @Autowired
    MLMetricsRepository mlmetricsRepo;

    @Autowired
    SpringRelationRepository springRelationRepo;

    @Autowired
    RelationRepository relationRepo;

    @Autowired
    SpringSimDataRepository springSimDataRepo;

    @Autowired
    SimDataRepository simDataRepo;

    @Autowired
    SpringTimingRepository springTimingRepo;

    @Autowired
    TimingRepository timingRepo;

    @Autowired
    SpringTimingBLRepository springTimingBLRepo;

    @Autowired
    TimingBLRepository timingBLRepo;

    public DbSpring() {
    }

    public List<StockItem> getAll(String market) throws Exception {
        long time0 = System.currentTimeMillis();
        try {
            List<StockItem> stocks = springStockRepo.findByMarketid(market).stream().map(e -> map(e)).toList();
            log.info("time0 {} {}", market, (System.currentTimeMillis() - time0));
            return stocks;
        } catch (Exception e) {
            return null;
        }
    }

    public List<MetaItem> getAll() throws Exception {
        long time0 = System.currentTimeMillis();
        List<MetaItem> metas = StreamSupport.stream(springMetaRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
        log.info("time0 " + (System.currentTimeMillis() - time0));
        return metas;
    }

    private AboveBelowItem map(AboveBelow data) {
        AboveBelowItem item = new AboveBelowItem();
        item.setComponents(data.getComponents());
        item.setDate(data.getDate());
        item.setMarket(data.getMarket());
        item.setRecord(data.getRecord());
        item.setScore(data.getScore());
        item.setSubcomponents(data.getSubcomponents());
        return item;
    }

    public AboveBelow map(AboveBelowItem item) {
        AboveBelow data = new AboveBelow();
        data.setComponents(item.getComponents());
        data.setDate(item.getDate());
        data.setMarket(item.getMarket());
        data.setRecord(item.getRecord());
        data.setScore(item.getScore());
        data.setSubcomponents(item.getSubcomponents());
        return data;
    }

    private ActionComponentItem map(ActionComponent ac) {
        ActionComponentItem item = new ActionComponentItem();
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

    public ActionComponent map(ActionComponentItem item) {
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

    public Config map(ConfigItem item) {
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
            config.setValue(JsonUtil.strip(item.getValue()).getBytes());
        }
        return config;
    }

    private ConfigItem map(Config config) {
        ConfigItem configItem = new ConfigItem();
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

    private Cont map(ContItem item) {
        Cont cont = new Cont();
        cont.setDate(item.getDate());
        cont.setFilename(item.getFilename());
        cont.setMd5(item.getMd5());
        return cont;
    }

    private ContItem map(Cont cont) {
        ContItem contItem = new ContItem();
        contItem.setDate(cont.getDate());
        contItem.setFilename(cont.getFilename());
        contItem.setMd5(cont.getMd5());
        return contItem;
    }

    private MetaItem map(Meta meta) {
        MetaItem item = new MetaItem();
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

    private Meta map(MetaItem meta) {
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

    private MetaItem map2(Meta meta) {
        return new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
    }

    private MLMetricsItem map(MLMetrics mltest) {
        MLMetricsItem mltestItem = new MLMetricsItem();
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

    public MLMetrics map(MLMetricsItem item) {
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

    private RelationItem map(Relation relation) {
        RelationItem relationItem = new RelationItem();
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

    public IncDec map(IncDecItem item) throws Exception {
        IncDec incdec = new IncDec();
        incdec.setComponent(item.getComponent());
        incdec.setDate(item.getDate());
        incdec.setDescription(item.getDescription());
        incdec.setId(item.getId());
        incdec.setIncrease(item.isIncrease());
        incdec.setLocalcomponent(item.getLocalcomponent());
        incdec.setMarket(item.getMarket());
        incdec.setName(item.getName());
        incdec.setParameters(item.getParameters());
        incdec.setRecord(item.getRecord());
        incdec.setScore(item.getScore());
        incdec.setSubcomponent(item.getSubcomponent());
        return incdec;
    }

    public Relation map(RelationItem item) throws Exception {
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

    public Memory map(MemoryItem item) throws Exception {
        Memory memory = new Memory();
        memory.setAction(item.getAction());
        memory.setAbovepositives(item.getAbovepositives());
        memory.setAbovesize(item.getAbovesize());
        memory.setBelowpositives(item.getBelowpositives());
        memory.setBelowsize(item.getBelowsize());
        memory.setCategory(item.getCategory());
        memory.setComponent(item.getComponent());
        memory.setConfidence(item.getConfidence());
        memory.setDate(item.getDate());
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
        memory.setFuturedate(item.getFuturedate());
        memory.setFuturedays(item.getFuturedays());
        memory.setInfo(item.getInfo());
        memory.setLearnConfidence(item.getLearnConfidence());
        memory.setLocalcomponent(item.getLocalcomponent());
        memory.setMarket(item.getMarket());
        memory.setPositives(item.getPositives());
        memory.setPosition(item.getPosition());
        memory.setRecord(item.getRecord());
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

    private SimDataItem map(SimData data) {
        SimDataItem item = new SimDataItem();
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

    public SimData map(SimDataItem item) {
        SimData data = new SimData();
        data.setConfig(item.getConfig().getBytes());
        data.setEnddate(item.getEnddate());
        if (item.getFilter() != null) {
            data.setFilter(item.getFilter().getBytes());
        }
        data.setMarket(item.getMarket());
        data.setRecord(item.getRecord());
        data.setScore(item.getScore());
        data.setStartdate(item.getStartdate());
        return data;
    }

    private StockItem map2(Stock stock) {
        try {
            return new StockItem(stock.getDbid(), stock.getMarketid(), stock.getId(), stock.getIsin(), stock.getName(), stock.getDate(), stock.getIndexvalue(), stock.getIndexvaluelow(), stock.getIndexvaluehigh(), stock.getIndexvalueopen(), stock.getPrice(), stock.getPricelow(), stock.getPricehigh(), stock.getPriceopen(), stock.getVolume(), stock.getCurrency(), stock.getPeriod1(), stock.getPeriod2(), stock.getPeriod3(), stock.getPeriod4(), stock.getPeriod5(), stock.getPeriod6(), stock.getPeriod7(), stock.getPeriod8(), stock.getPeriod9());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private Stock map(StockItem item) {
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
        stock.setPeriod1(item.getPeriod(0));
        stock.setPeriod2(item.getPeriod(1));
        stock.setPeriod3(item.getPeriod(2));
        stock.setPeriod4(item.getPeriod(3));
        stock.setPeriod5(item.getPeriod(4));
        stock.setPeriod6(item.getPeriod(5));
        stock.setPeriod7(item.getPeriod(6));
        stock.setPeriod8(item.getPeriod(7));
        stock.setPeriod9(item.getPeriod(8));
        return stock;
    }

    private StockItem map(Stock stock) {
        StockItem item = new StockItem();
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

    public Timing map(TimingItem item) throws Exception {
        Timing timing = new Timing();
        timing.setAction(item.getAction());
        timing.setBuy(item.getBuy());
        timing.setComponent(item.getComponent());
        timing.setDate(item.getDate());
        timing.setDescription(item.getDescription());
        timing.setEvolve(item.isEvolve());
        timing.setMarket(item.getMarket());
        timing.setMlmarket(item.getMlmarket());
        timing.setRecord(item.getRecord());
        timing.setTime(item.getMytime());
        timing.setScore(item.getScore());
        timing.setSubcomponent(item.getSubcomponent());
        timing.setParameters(item.getParameters());
        return timing;
    }

    public TimingBL map(TimingBLItem item) {
        TimingBL timing = new TimingBL();
        timing.setCount(item.getCount());
        timing.setId(item.getId());
        timing.setRecord(item.getRecord());
        return timing;
    }

    private TimingBLItem map(TimingBL timing) {
        TimingBLItem timingItem = new TimingBLItem();
        timingItem.setCount(timing.getCount());
        timingItem.setDbid(timing.getDbid());
        timingItem.setId(timing.getId());
        timingItem.setRecord(timing.getRecord());
        return timingItem;
    }

    public MetaItem getMarket(String market) throws Exception {
        Optional<Meta> metas = springMetaRepo.findById(market);
        if (metas.isEmpty()) {
            return null;
        }
        return map(metas.get());
    }

    @Deprecated
    public static Map<String, Object[]> doCalculationsArr(MyConfig conf, Map<String, Double[]> listMap, String key, Calculatable indicator, boolean wantPercentizedPriceIndex) {
        Map<String, Object[]> objectMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            Double [] list = listMap.get(id);
            if (wantPercentizedPriceIndex) {
                list = ArraysUtil.getPercentizedPriceIndex(list);
            }
            log.debug("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.debug("list " + list.length + " " + Arrays.asList(list));
            //double momentum = tu.getMom(list, conf.getDays());
            //Object[] objs = (Object[]) indicator.calculate(ArraysUtil.getNonNull(list));
            //objectMap.put(id, objs);
        }
        return objectMap;
    }
    public static Map<String, Object[]> doCalculationsArrNonNull(MyConfig conf, Map<String, double[][]> listMap, String key, Calculatable indicator, boolean wantPercentizedPriceIndex) {
        Map<String, Object[]> objectMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            double [][] list = listMap.get(id);
            if ("F00000HGSN".equals(id)) {              
                log.debug("braz " + Arrays.toString(list));                
            }
            /*
           if (wantPercentizedPriceIndex && list.length > 0 && list[0].length > 0) {
               double first = list[0][0];
               for(int i = 0; i < list.length; i ++)
                list[i] = ArraysUtil.getPercentizedPriceIndex(list[i], key, indicator.getCategory(), first);
            }
             */
            if ("2647727".equals(id)) {              
                log.debug("braz " + Arrays.toString(list));                
            }
            log.debug("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.debug("list " + list.length + " " + Arrays.asList(list));
            //double momentum = tu.getMom(list, conf.getDays());
            if (list.length == 180) {
                log.debug("180");
            } else {
                log.debug("not");
            }
            if (list[0].length == 0) {
                //continue;
            }
            Object[] objs = (Object[]) indicator.calculate(list);
            if ("F00000HGSN".equals(id)) {
                log.debug("braz " + Arrays.asList(list));
            }
            objectMap.put(id, objs);
        }
        return objectMap;
    }

    public List<MetaItem> getMetas() {
        //List l = springMetaRepo.findAll .findAll();
        //return StreamSupport.stream(springMetaRepo.findAll().spliterator(), false).toList();
        return StreamSupport.stream(springMetaRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
    }

    public List<String> getMarkets() {
        try {
            return stockRepo.getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public Object save(Object object) {
        try {
            Object obj2 = null;
            if (object instanceof AboveBelowItem obj) {
                obj2 = springAboveBelowRepo.save(map(obj));
            }
            if (object instanceof ActionComponentItem obj) {
                obj2 = springActionComponentRepo.save(map(obj));
            }
            if (object instanceof ConfigItem obj) {
                obj2 = springConfigRepo.save(map(obj));
            }
            if (object instanceof ContItem obj) {
                obj2 = springContRepo.save(map(obj));
            }
            if (object instanceof MemoryItem obj) {
                obj2 = springMemoryRepo.save(map(obj));
            }
            if (object instanceof MetaItem obj) {
                obj2 = springMetaRepo.save(map(obj));
            }
            if (object instanceof MLMetricsItem obj) {
                obj2 = springMLMetricsRepo.save(map(obj));
            }
            if (object instanceof RelationItem obj) {
                obj2 = springRelationRepo.save(map(obj));
            }
            if (object instanceof SimDataItem obj) {
                obj2 = springSimDataRepo.save(map(obj));
            }
            if (object instanceof StockItem obj) {
                obj2 = springStockRepo.save(map(obj));
            }
            if (object instanceof TimingItem obj) {
                obj2 = springTimingRepo.save(map(obj));
            }
            if (object instanceof TimingBLItem obj) {
                obj2 = springTimingBLRepo.save(map(obj));
            }
            if (object instanceof IncDecItem obj) {
                obj2 = springIncdecRepo.save(map(obj));
            }
            if (obj2 == null) {
                throw new Exception("Unknown save object");
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);

        }
        return null;
    }
    public void deleteById(Object object, String dbid) {
        try {
            if (object instanceof ActionComponentItem) {
                springActionComponentRepo.deleteById(Long.valueOf(dbid));
            }
            if (object instanceof TimingBLItem) {
                // not @id
                timingBLRepo.deleteById(dbid);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);

        }
    }
    public void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        try {
            if (object instanceof AboveBelowItem) {
                aboveBelowRepo.delete(market, startDate, endDate);

            }
            if (object instanceof IncDecItem) {
                incdecRepo.delete(market, null, component, subcomponent, startDate, endDate);
            }
            if (object instanceof MemoryItem) {
                memoryRepo.delete(market, component, subcomponent, startDate, endDate);

            }
            if (object instanceof TimingItem) {
                timingRepo.delete(market, action, component, subcomponent, startDate, endDate);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);

        }
    }

    public List<MLMetricsItem> getMLMetrics() {
        try {
            return mlmetricsRepo.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<MLMetricsItem> getMLMetrics(String market, Date startDate, Date endDate) {
        try {
            return mlmetricsRepo.getAll(market, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<ConfigItem> getConfigsByMarket(String market) {
        try {
            return configRepo.getAll(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<SimDataItem> getSimData(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        return simDataRepo.getAll(market, startDate, endDate);
    }

    public List<AboveBelowItem> getAboveBelow(String market, Date startDate, Date endDate) throws Exception {
        return aboveBelowRepo.getAll(market, startDate, endDate);
    }

    public List<ActionComponentItem> getActionComponent() {
        return StreamSupport.stream(springActionComponentRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
    }

    public List<TimingBLItem> getTimingBL() {
        return StreamSupport.stream(springTimingBLRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
    }

    public List<Date> getDates(String market) {
        try {
            return stockRepo.getDates(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<ConfigItem> getConfigs(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        try {
            return configRepo.getAll(market, action, component, subcomponent, parameters, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<IncDecItem> getIncDecs(String market, Date startDate, Date endDate, String parameters) {
        try {
            return incdecRepo.getAll(market, startDate, endDate, parameters);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<IncDecItem> getIncDecs() {
        try {
            return incdecRepo.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<RelationItem> getRelations() {
        try {
            return StreamSupport.stream(springRelationRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
            //return springRelationRepo.findAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<TimingItem> getTimings() {
        try {
            return timingRepo.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<TimingItem> getTimings(String market, String action, Date startDate, Date endDate) {
        try {
            return timingRepo.getAll(market, action, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<StockItem> getStocksByMarket(String market) {
        try {
            return stockRepo.getAll(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public MetaItem getMetaByMarket(String market) {
        try {
            Optional<Meta> o = springMetaRepo.findById(market);
            if (o.isPresent()) {
                return map(o.get());
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<MemoryItem> getMemories() {
        try {
            return memoryRepo.getAll(null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<MemoryItem> getMemoriesByMarket(String market) {
        try {
            return memoryRepo.getAll(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<MemoryItem> getMemories(String market, String action, String component, String subcomponent,
            String parameters, Date startDate, Date endDate) {
        try {
            return memoryRepo.getAll(market, action, component, subcomponent, parameters, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public List<ContItem> getCont() {
        try {
            return StreamSupport.stream(springContRepo.findAll().spliterator(), false).map(e -> map(e)).toList();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

}
