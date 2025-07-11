package roart.testdata;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import java.time.temporal.ChronoUnit;

import roart.common.model.MetaDTO;
import roart.iclij.config.ComponentConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.SimulateFilter;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.config.SimulateInvestUtils;
import roart.iclij.model.Parameters;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.model.IncDecDTO;
import roart.common.model.SimDataDTO;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.SerialVolume;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.db.dao.util.StockETL;
import roart.model.data.StockData;
import roart.etl.db.Extract;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;

public class TestData {
    private IclijConfig conf;

    String[] periods;
    
    public TestData() {
        super();
    }

    public TestData(IclijConfig conf) {
        this.conf = conf;
    }

    public Map<String, List<List<Double>>> getAbnormCatValMap() {
        Map<String, List<List<Double>>> map = new HashMap<>();
        List<Double> l = Arrays.asList(new Double[] { 1.0, 10.0, 1.0 } );
        List<List<Double>> l2 = new ArrayList<>();
        l2.add(l);
        map.put("1", l2);
        return map;
    }
    
    public Map<String, List<List<Double>>> getAbnormWithHolesCatValMap() {
        Map<String, List<List<Double>>> map = new HashMap<>();
        List<Double> l = Arrays.asList(new Double[] { 1.0, null, 10.0, 1.0 } );
        List<List<Double>> l2 = new ArrayList<>();
        l2.add(l);
        map.put("1", l2);
        return map;
    }
    
    public List<String> getStockDates(LocalDate startdate, int num, boolean forward) {
        List<String> list = new ArrayList<>();
        // TODO weekday setting from config
        LocalDate date = startdate;
        while (num > 0) {
            if (TimeUtil.isWeekday(TimeUtil.convertDate(date))) {
                list.add(TimeUtil.convertDate2(date));
                num--;
            }
            if (forward) {
                date = date.plusDays(1);
            } else {
                date = date.minusDays(1);
            }
        }
        Collections.sort(list);
        return list;
    }
    
    public List<String> getStockDates(LocalDate startdate, LocalDate enddate) {
        List<String> list = new ArrayList<>();
        // TODO weekday setting from config
        for (LocalDate date = startdate; date.isBefore(enddate); date = date.plusDays(1)) {
            if (TimeUtil.isWeekday(TimeUtil.convertDate(date))) {
                list.add(TimeUtil.convertDate2(date));
            }
        }
        Collections.sort(list);
        return list;
    }

    public Map<String, List<List<Double>>> getVolumeCatValMap() {
        Map<String, List<List<Double>>> map = new HashMap<>();
        List<Double> l = Arrays.asList(new Double[] { 4320.0, null, null, null, null, null, null, null, null, null, null, null, null, 4200.0 } );
        List<List<Double>> l2 = new ArrayList<>();
        l2.add(l);
        map.put("1", l2);
        return map;
    }
    
    public Map<String, Long[]> getVolumeMap() {
        Map<String, Long[]> map = new HashMap<>();
        Long nulls = null;
        Long t = 3L;
        Long[] l = new Long[] { t, nulls, nulls, nulls, nulls, nulls, nulls, nulls, nulls, nulls, nulls, nulls, nulls, t};
        //List<List<Object>> l2 = new ArrayList<>();
        //l2.add(l);
        map.put("1", l);
        return map;
    }
    
    public Map<String, Double[][]> getListMap() {
        Map<String, Double[][]> aListMap = new HashMap<>();
        aListMap.put("id1", new Double[][] { { 1.0 , 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0 } });
        aListMap.put("id2", new Double[][] { { 11.0 , 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0, 40.0, 41.0, 42.0, 43.0, 44.0, 45.0 } });
        //aListMap.put("id1", new Double[][] { { 1.0 , 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 } });
        //aListMap.put("id2", new Double[][] { { 11.0 , 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0  } });
        return aListMap;
    }

    public List<StockDTO> getStockDTO(String market, boolean weekdays, Double[] data, int period) throws Exception {
        List<StockDTO> list = new ArrayList<>();
        LocalDate localDate = LocalDate.now().minusDays(60);
        localDate = TimeUtil.add(localDate, weekdays);
        Date mydate = TimeUtil.convertDate(localDate);
        list.addAll(getStockDTO(market, "id1", mydate, weekdays, new Double[] { 1.0 , 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0 }, period));
        list.addAll(getStockDTO(market, "id2", mydate, weekdays, new Double[] { 11.0 , 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0, 27.0, 28.0, 29.0, 30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0, 37.0, 38.0, 39.0, 40.0, 41.0, 42.0, 43.0, 44.0, 45.0 }, period));
        return list;
    }

    public List<StockDTO> getStockDTO(String market, String id, Date date, boolean weekdays, Double[] data, int period) throws Exception {
        List<StockDTO> list = new ArrayList<>();
        LocalDate mydate = TimeUtil.convertDate(date);
        for (Double datum : data) {
            StockDTO stock = new StockDTO();
            stock.setMarketid(market);
            stock.setId(id);
            stock.setName("name"+id);
            stock.setDate(TimeUtil.convertDate(mydate));
            switch (period) {
            case Constants.INDEXVALUECOLUMN:
                stock.setIndexvalue(datum);
                break;
            case Constants.PRICECOLUMN:
                stock.setPrice(datum);
                break;
            default:
                stock.setPeriod(period, datum);
            }
            list.add(stock);
            mydate = TimeUtil.add(mydate, weekdays);
        }
        return list;
    }
    
    public StockData getStockdata(IclijConfig conf) throws Exception {
        List<StockDTO> stocks = getStockDTO(TestConstants.MARKET, true, null, Constants.INDEXVALUECOLUMN);
        MetaDTO meta = new MetaDTO(TestConstants.MARKET, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        String[] periodText = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        return new Extract((DbDao)null).getStockData(conf, TestConstants.MARKET, stocks, meta, periodText);
    }
    
    public StockData getStockdata2(IclijConfig conf) throws Exception {
        List<StockDTO> stocks = getStockDTO(TestConstants.MARKET2, true, null, Constants.INDEXVALUECOLUMN);
        MetaDTO meta = new MetaDTO(TestConstants.MARKET2, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        String[] periodText = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        return new Extract((DbDao)null).getStockData(conf, TestConstants.MARKET2, stocks, meta, periodText);
    }
    
    public StockData getStockdata3(IclijConfig conf) throws Exception {
        List<StockDTO> stocks = getStockDTO(TestConstants.MARKET3, true, null, Constants.INDEXVALUECOLUMN);
        MetaDTO meta = new MetaDTO(TestConstants.MARKET3, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        String[] periodText = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        return new Extract((DbDao)null).getStockData(conf, TestConstants.MARKET3, stocks, meta, periodText);
    }

    public Map<String, StockData> getExtraStockdataMap(IclijConfig conf) throws Exception {
        Map<String, StockData> map = new HashMap<>();
        map.put(TestConstants.MARKET2, getStockdata2(conf));
        map.put(TestConstants.MARKET3, getStockdata3(conf));
        return map;
    }

    public StockData getStockdata(IclijConfig conf, Date startDate, Date endDate, String marketName, int size, int column, boolean ohlc) throws Exception {
        List<StockDTO> stocks = getStockDTO(startDate, endDate, marketName, size, true, column, ohlc, new String[0], null);
        MetaDTO meta = new MetaDTO(marketName, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        String[] periodText = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        return new Extract((DbDao)null).getStockData(conf, marketName, stocks, meta, periodText);
    }
    
    public StockData getStockdata(IclijConfig conf, Date startDate, Date endDate, String market, int size, boolean weekdays, int period, boolean ohlc) throws Exception {
        List<StockDTO> stocks = getStockDTO(startDate, endDate, market, size, weekdays, period, ohlc, new String[0], null);
        MetaDTO meta = new MetaDTO(TestConstants.MARKET, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        String[] periodText = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        return new Extract((DbDao)null).getStockData(conf, market, stocks, meta, periodText);
    }
    
    public List<StockDTO> getStockDTO(Date startDate, Date endDate, String market, int size, boolean weekdays, int period, boolean ohlc, String[] periods, String idTemplate) throws Exception {
        this.periods = periods;
        Random random = new Random();
        List<StockDTO> list = new ArrayList<>();
        LocalDate startdate = TimeUtil.convertDate(startDate);
        LocalDate enddate = TimeUtil.convertDate(endDate);
        long days = Duration.between(startdate.atStartOfDay(), enddate.atStartOfDay()).toDaysPart();
        for (int i = 0; i < size; i++) {
            String id;
            if (idTemplate == null) {
                id = UUID.randomUUID().toString();
            } else {
                id = idTemplate + i;
            }
            double split = random.nextDouble();
            int startsplit = (int) (days * split);
            int endsplit = (int) (days - startsplit);
            LocalDate mystartdate = startdate;
            if (startsplit > 0 && random.nextLong(10) < 2) {
                mystartdate = mystartdate.plus(random.nextLong(startsplit), ChronoUnit.DAYS);                
                mystartdate = TimeUtil.add(mystartdate, weekdays);
            }
            LocalDate myenddate = enddate;
            if (startsplit > 0 && random.nextLong(10) < 2) {
                myenddate = myenddate.minus(random.nextLong(startsplit), ChronoUnit.DAYS);
                myenddate = TimeUtil.add(myenddate, weekdays);
            }
            double datum = random.nextDouble(1000);
            while (mystartdate.isBefore(myenddate)) {
                StockDTO stock = new StockDTO();
                stock.setMarketid(market);
                stock.setId(id);
                stock.setName("name"+id);
                stock.setDate(TimeUtil.convertDate(mystartdate));
                boolean missing = random.nextInt(100) < 2;
                // TODO for all
                if (!missing) {
                    for (int j = 0; j < periods.length; j++) {
                        stock.setPeriod(j, datum + j);                        
                    }
                    switch (period) {
                    case Constants.INDEXVALUECOLUMN:
                        stock.setIndexvalue(datum);
                        break;
                    case Constants.PRICECOLUMN:
                        stock.setPrice(datum);
                        if (ohlc) {
                            stock.setPricelow(change(datum, -1, 0.04, random));
                            stock.setPriceopen(change(datum, -1, 0.04, random));
                            stock.setPricehigh(change(datum, 1, 0.04, random));
                        }
                        break;
                    default:
                        stock.setPeriod(period, datum);
                    }
                    list.add(stock);
                }
                mystartdate = TimeUtil.add(mystartdate, weekdays);
                datum = change(random, datum);
                // TODO random big change
            }
        }
        list = StockETL.filterWeekend(conf, list);
        return list;
    }

    private Double change(double datum, int i, double d, Random random) {
        datum = datum * (1.0 + i * d * random.nextDouble());
        return datum;
    }

    private double change(Random random, double datum) {
        datum = datum * (1.02 - 0.04 * random.nextDouble());
        return datum;
    }
    
    public List<MetaDTO> getMetas(String market) {
        MetaDTO meta = new MetaDTO(market, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        return List.of(meta);
    }
    
    public List<MetaDTO> getMetas() {
        MetaDTO meta = new MetaDTO(TestConstants.MARKET, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", null, null, null);
        return List.of(meta);
    }

    public List<MetaDTO> getMetas(String marketName, String[] periods, boolean ohlc) {
        String[] p = new String[] { "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9" };
        for (int i = 0; i < periods.length; i++) {
            p[i] = periods[i];
        }
        MetaDTO meta = new MetaDTO(marketName, p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], null, null, ohlc);
        return List.of(meta);
    }

    public List<IncDecDTO> incdec(List<StockDTO> all) {
        Parameters parameters = new Parameters();
        parameters.setThreshold(1.0);
        parameters.setFuturedays(10);
        
        List<IncDecDTO> list = new ArrayList<>();
        StockDTO stock = all.get(0);
        IncDecDTO item = new IncDecDTO();
        item.setDate(LocalDate.now());
        item.setMarket(stock.getMarketid());
        item.setIncrease(true);
        item.setId(stock.getId());
        item.setName(stock.getName());
        item.setScore(0.5);
        item.setParameters(JsonUtil.convert(parameters));
        item.setComponent(PipelineConstants.MLRSI);
        item.setSubcomponent(MLConstants.TENSORFLOW + " " + MLConstants.GRU);
        // no localcomponent
        list.add(item);
        return list;
    }
    
    public List<SimulateInvestConfig> getSim(String market, Date startDate, Date endDate, IclijConfig config, int num) {
        List<SimulateInvestConfig> list = new ArrayList<>();
        List<String> confList = ComponentConstants.getSimulateInvestConfig();
        IclijConfigMapGene gene = new IclijConfigMapGene(confList , config);
        for (int i = 0; i < num; i++) {
            gene.randomize();
            IclijConfig aConf = config.copy();
            aConf.getConfigData().getConfigValueMap().putAll(gene.getMap());
            SimulateInvestConfig sim = SimulateInvestUtils.getSimConfig(aConf);
            list.add(sim);
        }
        System.out.println("Sim sample " + list.get(0).asValuedMap());
        return list;
    }
    
    public List<SimDataDTO> getSimData(String market, Date startDate, Date endDate, IclijConfig config, int num) {
        List<SimDataDTO> list = new ArrayList<>();
        Random random = new Random();
        LocalDate aStartDate = TimeUtil.convertDate(startDate);
        LocalDate anEndDate = TimeUtil.convertDate(endDate);
        Period period = Period.between(aStartDate, anEndDate);
        int months = period.getMonths() + period.getYears() * 12;
        SimulateFilter filter = new SimulateFilter(6, 0.9, 0.0, 0.0, false, 20, false, null);
        //filter.setCorrelation(0.9);

        List<SimulateInvestConfig> sims = getSim(market, startDate, endDate, config, num);
        
        long l = 0;
        for (SimulateInvestConfig sim : sims) {
            LocalDate anotherStartDate = aStartDate.plusMonths(random.nextLong(months - 3));
            LocalDate anotherEndDate = anotherStartDate.plusMonths(1);
            sim.setStartdate(TimeUtil.convertDate2(anotherStartDate));
            sim.setEnddate(TimeUtil.convertDate2(anotherEndDate));
            SimDataDTO simdata = new SimDataDTO();
            simdata.setDbid(l++);
            simdata.setRecord(LocalDate.now());
            simdata.setStartdate(anotherStartDate);
            simdata.setEnddate(anotherEndDate);
            simdata.setMarket(market);
            simdata.setConfig(JsonUtil.convert(sim.asValuedMap()));
            simdata.setScore(random.nextDouble(5));
            simdata.setFilter(JsonUtil.convert(filter));
            list.add(simdata);
        }
        return list;
    }
}
