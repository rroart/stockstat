package roart.iclij.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.model.IncDecItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.TwoDimD;
import roart.common.util.MapUtil;
import roart.common.util.PipelineUtils;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.config.ActionComponentConfig;
import roart.service.model.ProfitData;

public class MarketUtil {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Market findMarket(String market, IclijConfig iclijConfig) {
        List<Market> markets = getMarkets(iclijConfig);
        Market foundMarket = null;
        for (Market aMarket : markets) {
            if (market.equals(aMarket.getConfig().getMarket())) {
                foundMarket = aMarket;
                break;
            }
        }
        return foundMarket;
    }
    
    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    public List<Market> getMarkets(boolean isDataset, IclijConfig iclijConfig) {
        List<Market> markets = new ArrayList<>();
        try { 
            markets = IclijXMLConfig.getMarkets(iclijConfig);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        markets = new MarketUtil().filterMarkets(markets, isDataset);
        return markets;
    }

    public List<Market> filterMarkets(List<Market> markets, boolean isDataset) {
        List<Market> filtered = new ArrayList<>();
        for (Market market : markets) {
            Boolean dataset = market.getConfig().getDataset();
            boolean adataset = dataset != null && dataset;
            if (adataset == isDataset) {
                filtered.add(market);
            }
        }
        return filtered;
    }

    public List<MemoryItem> getMarketMemory(Market market, IclijDbDao dbDao) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = dbDao.getAll(market.getConfig().getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    public List<MemoryItem> getMarketMemory(Market market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate, IclijDbDao dbDao) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = dbDao.getAllMemories(market.getConfig().getMarket(), action, component, subcomponent, parameters, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    public short getStartoffset(Market market) {
        Short startoffset = market.getConfig().getStartoffset();
        return startoffset != null ? startoffset : 0;
    }

    public void filterIncDecs(ComponentData param, Market market, ProfitData profitdata,
            PipelineData[] maps, boolean inc, List<String> mydates) {
        List<String> dates;
        if (mydates == null) {
            dates = param.getService().getDates(param.getService().conf.getConfigData().getMarket());        
        } else {
            dates = mydates;
        }
        String category;
        if (inc) {
            category = market.getFilter().getInccategory();
        } else {
            category = market.getFilter().getDeccategory();
        }
        if (category != null) {
            PipelineData categoryMap = PipelineUtils.getPipeline(maps, category);
            if (categoryMap != null) {
                Integer offsetDays = null;
                Integer days;
                if (inc) {
                    days = market.getFilter().getIncdays();
                } else {
                    days = market.getFilter().getDecdays();
                }
                if (days != null) {
                    if (days == 0) {
                        int year = param.getInput().getEnddate().getYear();
                        year = param.getFutureDate().getYear();
                        List<String> yearDates = new ArrayList<>();
                        for (String date : dates) {
                            int aYear = Integer.valueOf(date.substring(0, 4));
                            if (year == aYear) {
                                yearDates.add(date);
                            }
                        }
                        Collections.sort(yearDates);
                        String oldestDate = yearDates.get(0);
                        int index = dates.indexOf(oldestDate);
                        offsetDays = dates.size() - 1 - index;
                    } else {
                       offsetDays = days;
                    }
                }
                Double threshold;
                if (inc) {
                    threshold = market.getFilter().getIncthreshold();
                } else {
                    threshold = market.getFilter().getDecthreshold();
                }
                Map<String, List<List>> listMap3 = getCategoryList(maps, category);
                Map<String, IncDecItem> buysFilter = incdecFilterOnIncreaseValue(market, inc ? profitdata.getBuys() : profitdata.getSells(), maps, threshold, categoryMap,
                        listMap3, offsetDays, inc);
                if (inc) {
                    profitdata.setBuys(buysFilter);
                } else {
                    profitdata.setSells(buysFilter);
                }
            }
        }
    }
    
    public Map<String, IncDecItem> incdecFilterOnIncreaseValue(Market market, Map<String, IncDecItem> incdecs,
            PipelineData[] maps, Double threshold, PipelineData categoryMap,
            Map<String, List<List>> listMap3, Integer offsetDays, boolean inc) {
        Map<String, IncDecItem> incdecsFilter = new HashMap<>();
        for(IncDecItem item : incdecs.values()) {
            String key = item.getId();
            if (listMap3 == null) {
                if (categoryMap != null) {
                    log.debug("" + categoryMap.keySet());
                }
                if (maps != null) {
                    //System.out.println(maps.keySet());
                }
                log.debug("market null map {}", market.getConfig().getMarket());
                continue;
            }
            List<List> list = listMap3.get(key);
            if (list == null) {
                continue;
            }
            List<Double> list0 = list.get(0);
            Double value = null;
            if (offsetDays == null) {
                value = list0.get(list0.size() - 1);
                if (value != null) {
                    value = 1 + (value / 100);
                }
            } else {
                Double curValue = list0.get(list0.size() - 1);
                Double oldValue = list0.get(list0.size() - 1 - offsetDays);
                if (curValue != null && oldValue != null) {
                    value = curValue / oldValue;
                }
            }
            if (value == null || threshold == null) {
                continue;
            }
            if (inc && value < threshold) {
                continue;
            }
            if (!inc && value > threshold) {
                continue;
            }
            incdecsFilter.put(key, item);
        }
        return incdecsFilter;
    }

    public Map<String, List<List>> getCategoryList(PipelineData[] maps, String category) {
        String newCategory = null;
        if (Constants.PRICE.equals(category)) {
            newCategory = "" + Constants.PRICECOLUMN;
        }
        if (Constants.INDEX.equals(category)) {
            newCategory = "" + Constants.INDEXVALUECOLUMN;
        }
        if (newCategory != null) {
            PipelineData map = PipelineUtils.getPipeline(maps, newCategory);
            return (Map) MapUtil.convertA2L(PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) map.get(PipelineConstants.LIST)));
        }
        Map<String, List<List>> listMap3 = null;
        for (Entry<String, PipelineData> entry : PipelineUtils.getPipelineMap(maps).entrySet()) {
            PipelineData map = entry.getValue();
            if (category.equals(map.get(PipelineConstants.CATEGORYTITLE))) {
                listMap3 = (Map<String, List<List>>) map.get(PipelineConstants.LIST);
            }
        }
        return listMap3;
    }

    public void fillProfitdata(ProfitData profitdata, List<IncDecItem> incdecitems) {
        for (IncDecItem item : incdecitems) {
            String id = item.getId() + item.getDate().toString();
            if (item.isIncrease()) {
                profitdata.getBuys().put(id, item);
            } else {
                profitdata.getSells().put(id, item);
            }
        }
    }
    
}
