package roart.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.math3.util.Pair;

import roart.component.model.ComponentInput;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.constants.RelationConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.RelationItem;
import roart.iclij.model.TimingItem;
import roart.iclij.service.IclijServiceList;

public class RelationUtil {
    public List[] method(ComponentInput componentInput, List<IncDecItem> listIncDecs) throws Exception {
        LocalDate date = componentInput.getEnddate();
        IclijXMLConfig conf = IclijXMLConfig.instance();
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<IncDecItem> listAll = IncDecItem.getAll();
        /*
        List<IclijServiceList> lists = new ArrayList<>();
        lists.add(ServiceUtil.getHeader("Content"));
        
        Map<String, List<IncDecItem>> incMap = new HashMap<>();
        Map<String, List<IncDecItem>> decMap = new HashMap<>();
        Map<String, List<IncDecItem>> incDecMap = new HashMap<>();
        
        Map<Pair<String, String>, IncDecItem> incPairMap= new HashMap<>();
        Map<Pair<String, String>, IncDecItem> decPairMap= new HashMap<>();
        Map<Pair<String, String>, IncDecItem> incDecPairMap= new HashMap<>();
        */
        /*

    rel: market, id/null
    
    total id: market, id
       partof
       equivalent
         
         
         

         */
        /*
        List<Market> markets = conf.getMarkets(instance);
        for (Market market : markets) {
            List<IncDecItem> currentIncDecs = getCurrentIncDecs(date, listAll, market);
            List<IncDecItem> listInc = currentIncDecs.stream().filter(m -> m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listDec = currentIncDecs.stream().filter(m -> !m.isIncrease()).collect(Collectors.toList());
            List<IncDecItem> listIncDec = ServiceUtil.moveAndGetCommon(listInc, listDec);
            
            String marketName = market.getConfig().getMarket();
            incMap.put(marketName, listInc);
            decMap.put(marketName, listDec);
            incDecMap.put(marketName, listIncDec);

            List<IncDecItem> list = listInc;
            Map<Pair<String, String>, IncDecItem> map = incPairMap;
            
            mapPutter(listInc, incPairMap);
            mapPutter(listDec, decPairMap);
            mapPutter(listIncDec, incDecPairMap);
            
            //List<IclijServiceList> subLists = ServiceUtil.getServiceList(market.getConfig().getMarket(), listInc, listDec, listIncDec);
            //lists.addAll(subLists);
        }
        */
        
        List<RelationItem> relations = IclijDbDao.getAllRelations();
        
        //List<RelationItem> foundRelations = search(listAll, relations);
        
        //List<TimingItem> listAllTimings = TimingItem.getAll();
        //List<TimingItem> currentTimings = getCurrentTimings(date, listAllTimings, IclijConstants.IMPROVEPROFIT);
        //List<IncDecItem> listAll = IncDecItem.getAll();
        List<IncDecItem> currentIncDecs = new ArrayList<>();
        currentIncDecs.addAll(listIncDecs);
        List<Market> markets = conf.getMarkets(instance);
        for (Market market : markets) {
            List<IncDecItem> marketCurrentIncDecs = ServiceUtil.getCurrentIncDecs(date, listAll, market, market.getFilter().getRecordage());
            currentIncDecs.addAll(marketCurrentIncDecs);
        }
        
        List<RelationItem> alreadyFound = new ArrayList<>();
        
        for (IncDecItem incdec : currentIncDecs) {
            RelationItem relation = new RelationItem();
            relation.setMarket(incdec.getMarket());
            relation.setId(incdec.getId());
            //relation.setOtherMarket(incdec.getMarket());
            //relation.setOtherId(incdec.getId());
            boolean done = false;
            while (!done) {
                List<RelationItem> foundRelations2 = searchPartof(relation, relations);
                foundRelations2.removeAll(alreadyFound);
                alreadyFound.addAll(foundRelations2);
                List<RelationItem> foundRelations4 = new ArrayList<>();
                for (RelationItem aRelation : foundRelations2) {
                    List<RelationItem> foundRelations3 = searchEquivalent(aRelation, relations);
                    foundRelations3.removeAll(alreadyFound);
                    alreadyFound.addAll(foundRelations3);
                    foundRelations4.addAll(foundRelations3);
                }
                foundRelations2 = foundRelations4;
                done = foundRelations2.isEmpty();
            }
        }
        
        alreadyFound = filter(currentIncDecs, alreadyFound);
        currentIncDecs = filter2(alreadyFound, currentIncDecs);
        
        List[] retObjects = new ArrayList[2];
        retObjects[0] = currentIncDecs;
        retObjects[1] = alreadyFound;
        return retObjects;
    }

    private List<IncDecItem> filter2(List<RelationItem> relations, List<IncDecItem> currentIncDecs) {
        List<IncDecItem> retain = new ArrayList<>();
        // partof
        for (RelationItem aRelation : relations) {
            String market = aRelation.getMarket();
            String id = aRelation.getId();
            String othermarket = aRelation.getOtherMarket();
            String otherid = aRelation.getOtherId();
            for (IncDecItem item : currentIncDecs) {
                if (market.equals(item.getMarket())) {
                    if (item.getId() != null && item.getId().equals(id)) {
                        retain.add(item);
                    }
                }
                if (othermarket.equals(item.getMarket())) {
                    if (item.getId() != null && item.getId().equals(otherid)) {
                        retain.add(item);
                    }
                }
            }
            // equivalent
        }
        return retain;
    }

    private List<RelationItem> filter(List<IncDecItem> currentIncDecs, List<RelationItem> relations) {
        List<RelationItem> retain = new ArrayList<>();
        for (IncDecItem item : currentIncDecs) {
            String market = item.getMarket();
            String id = item.getId();
            // partof
            for (RelationItem aRelation : relations) {
                if (!market.equals(aRelation.getMarket())) {
                    continue;
                }
                if (aRelation.getId() != null && aRelation.getId().equals(id)) {
                    retain.add(aRelation);
                }
                if (aRelation.getOtherId() != null && aRelation.getOtherId().equals(id)) {
                    retain.add(aRelation);
                }
            }
            // equivalent
        }
        return retain;
    }

    private List<RelationItem> searchEquivalent(RelationItem relation, List<RelationItem> relations) {
        List<RelationItem> retList = new ArrayList<>();

        String market = relation.getMarket();
        String id = relation.getId();
        
        for (RelationItem aRelation : relations) {
            if (aRelation.getType().equals(RelationConstants.EQUIVALENT)) {
                if (market.equals(aRelation.getMarket())) {
                    if (aRelation.getId() == null || aRelation.getId().equals(id)) {
                        retList.add(aRelation);
                    }
                }
                if (market.equals(aRelation.getOtherMarket())) {
                    if (aRelation.getOtherId() == null || aRelation.getOtherId().equals(id)) {
                        retList.add(aRelation);
                    }
                }
            }
        }

        return retList;
    }

    private List<RelationItem> search(IncDecItem incdec, List<RelationItem> relations) {
        List<RelationItem> retList = new ArrayList<>();
        String market = incdec.getMarket();
        String id = incdec.getId();
        
        for (RelationItem relation : relations) {
            if (relation.getType().equals(RelationConstants.PARTOF)) {
                if (!market.equals(relation.getMarket())) {
                    continue;
                }
                if (relation.getId() == null || relation.getId().equals(id)) {
                    retList.add(relation);
                }
            }
        }
        
        return retList;
    }

    private List<RelationItem> searchPartof(RelationItem relation, List<RelationItem> relations) {
        List<RelationItem> retList = new ArrayList<>();
        String market = relation.getMarket();
        String id = relation.getId();
        
        for (RelationItem aRelation : relations) {
            if (aRelation.getType().equals(RelationConstants.PARTOF)) {
                if (!market.equals(aRelation.getMarket())) {
                    continue;
                }
                if (aRelation.getId() == null || aRelation.getId().equals(id)) {
                    retList.add(aRelation);
                }
            }
        }
        
        return retList;
    }

    private List<RelationItem> search(List<IncDecItem> listAll, List<RelationItem> relations) {
        
        for (RelationItem i : relations) {
            
        }
        List<RelationItem> retList = new ArrayList<>();
        for (IncDecItem i : listAll) {
            
        }
        return null;
    }

    private void mapPutter(List<IncDecItem> list, Map<Pair<String, String>, IncDecItem> map) {
        for (IncDecItem item : list) {
            Pair<String, String> pair = new Pair(item.getMarket(), item.getId());
            map.put(pair, item);
        }
    }
    
    @Deprecated
    public static List<IncDecItem> getCurrentIncDecs(LocalDate date, List<IncDecItem> listAll, Market market2) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(10);
        List<IncDecItem> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        List<IncDecItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        //currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

    @Deprecated
    public static List<TimingItem> getCurrentTimings(LocalDate date, List<TimingItem> listAll, String action) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate newdate = date;
        LocalDate olddate = date.minusDays(10);
        List<TimingItem> filterListAll = listAll.stream().filter(m -> m.getRecord() != null).collect(Collectors.toList());
        filterListAll = filterListAll.stream().filter(m -> action.equals(m.getAction())).collect(Collectors.toList());
        List<TimingItem> currentIncDecs = filterListAll.stream().filter(m -> olddate.compareTo(m.getRecord()) <= 0).collect(Collectors.toList());
        currentIncDecs = currentIncDecs.stream().filter(m -> newdate.compareTo(m.getRecord()) >= 0).collect(Collectors.toList());
        //currentIncDecs = currentIncDecs.stream().filter(m -> market.getConfig().getMarket().equals(m.getMarket())).collect(Collectors.toList());
        return currentIncDecs;
    }

}
