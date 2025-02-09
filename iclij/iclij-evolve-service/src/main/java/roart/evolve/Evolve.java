package roart.evolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
// TODO serial  and sim
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.config.ConfigConstants;
import roart.common.config.Extra;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyXMLConfig;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.ml.MLMapsML;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.model.AboveBelowItem;
import roart.common.model.ActionComponentItem;
import roart.common.model.ConfigItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.TimingItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialScoreChromosome;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;
import roart.common.util.TimeUtil;
import roart.db.dao.IclijDbDao;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome;
import roart.filesystem.FileSystemDao;
import roart.gene.NeuralNetConfigGene;
import roart.iclij.evolution.marketfilter.chromosome.impl.AboveBelowChromosome;
import roart.iclij.evolution.marketfilter.chromosome.impl.MarketFilterChromosome2;
import roart.iclij.model.Parameters;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.constants.IclijConstants;
import roart.gene.impl.ConfigMapGene;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.inmemory.model.Inmemory;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.util.ServiceConnectionUtil;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class Evolve {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public static CuratorFramework curatorClient;

    private IclijConfig iclijConfig;

    private IclijDbDao dbDao;

    private FileSystemDao fileSystemDao;

    private Function<String, Boolean> zkRegister;
    
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    public Evolve(IclijDbDao dbDao, IclijConfig iclijConfig, FileSystemDao fileSystemDao) {
        this.iclijConfig = iclijConfig;
        this.dbDao = dbDao;
        this.fileSystemDao = fileSystemDao;
    }
    
    public void handleEvolve(String param) {
        
        //param = getParam(param);
        List<String> output = new ArrayList<>();
        TypeReference ref = new TypeReference<List<LinkedHashMap<Double, AbstractChromosome>>>(){};
        PipelineData data = JsonUtil.convertnostrip(param, PipelineData.class);
        String id = PipelineUtils.getString(data, EvolveConstants.ID);
        // TODO_
        List<SerialScoreChromosome> myList0 = PipelineUtils.getList(data, id);
        if (myList0 == null) {
        	return;
        }
        List<ImmutablePair<Double, String>> myList = myList0.stream().map(e -> new ImmutablePair<Double, String>(e.getLeft(), "" + e.getRight())).collect(Collectors.toList());
        //Map<String, Object> aconf = PipelineUtils.getMap(data, EvolveConstants.DEFAULT);
        //System.out.println("aconf" + aconf);
        //if (true) return;
        String title = PipelineUtils.getString(data, EvolveConstants.TITLETEXT);
        String[] parts = title.split(" ");
        String market = parts[1];
        String component = parts[2];
        String myclass = parts[3];
        NeuralNetConfig conf = new NeuralNetConfigs().getClass(myclass);
        Pair<String, String> subcomponent = new NeuralNetConfigs().getSubcomponent(myclass);
        List<MLMetricsItem> mltests = null;
        try {
            mltests = dbDao.getAllMLMetrics(market, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> met = getMLMetrics(mltests, null);
        //System.out.println("co" + component);
        //System.out.println("su" + subcomponent);
        //System.out.println("met" + met.keySet());
        List<MLMetricsItem> blbl = met.get(new ImmutablePair(component, subcomponent.getLeft() + " " + subcomponent.getRight()));
        double avg = 0;
        if (blbl != null) {
            avg = blbl.stream().map(MLMetricsItem::getTestAccuracy).mapToDouble(e -> e).average().orElse(0);
        }
        // TODO
        double newer = myList.get(0).getLeft();
        boolean better = avg < newer;
        myList.add(new ImmutablePair(avg, conf + " (default)"));
        Comparator<Pair> comparator = new Comparator<>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return Double.valueOf((Double)o1.getLeft()).compareTo(Double.valueOf((Double)o2.getLeft()));
            }
        };
        Collections.sort(myList, comparator);
        Collections.reverse(myList);
        for (ImmutablePair<Double, String> my : myList) {
            output.add(my.getLeft() + " " + my.getRight());
        }
        output.add("");
        output.add("Summary: " + better + " " + MathUtil.round(avg, 2) + " vs " + newer);
        String text = printtext(ServiceConstants.EVOLVEFILTEREVOLVE + " " + title, "File " + id, output);
        print(text);
        if (better) {
            NeuralNetChromosome c = (NeuralNetChromosome) myList0.get(0).getRight();
            NeuralNetConfigGene conf2 = c.getNnConfig();
            String ml = new MLMapsML().getMap().get(subcomponent);
            String key = new NeuralNetConfigs().getConfigMap().get(ml);
            if (!IclijConfigConstants.DATASET.equals(component)) {
            saveBetter(market, component, subcomponent, IclijConfigConstants.MACHINELEARNING, myList.get(0).getLeft(), key, conf2.getConfig(), true, null);
            } else {
            saveBetter(market, component, subcomponent, IclijConfigConstants.DATASET, myList.get(0).getLeft(), key, conf2.getConfig(), false, null);
            }
        }
    }

    private void saveBetter(String market, String component,
            Pair<String, String> subcomponent, String action, Double score, String key, Object object, boolean domct, String otherAction) {
        Parameters p = new Parameters();
        p.setFuturedays(10);
        p.setThreshold(1.0);
        ConfigItem i = new ConfigItem();
        i.setMarket(market);
        i.setComponent(component);
        i.setSubcomponent(subcomponent.getLeft() + " " + subcomponent.getRight());
        i.setId(key);
        i.setAction(action);
        i.setParameters(JsonUtil.convert(p));
        i.setRecord(LocalDate.now());
        i.setDate(LocalDate.now());
        i.setScore(score);
        String value = JsonUtil.convert(object);
        i.setValue(value);            
        try {
            dbDao.save(i);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (!domct) {
        	return;
        }
        // save to markettime
        ActionComponentItem mct = new ActionComponentItem();
        if (otherAction != null) {
        	mct.setAction(otherAction);
        } else {
        mct.setAction(action);
        }
        mct.setMarket(market);
        mct.setComponent(component);
        mct.setSubcomponent(subcomponent.getLeft() + " " + subcomponent.getRight());
        mct.setRecord(LocalDate.now());
        if (otherAction != null) {
            mct.setPriority(-20);
        } else {
        mct.setPriority(-10);
        }
        mct.setParameters(JsonUtil.convert(p));
        try {
            dbDao.save(mct);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    // dup
    protected Map<Pair<String, String>, List<MLMetricsItem>> getMLMetrics(List<MLMetricsItem> mltests, Double confidence) {
        List<MLMetricsItem> returnedMLMetrics = new ArrayList<>();
        for (MLMetricsItem test : mltests) {
            addNewest(returnedMLMetrics, test, 0.0);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> moreReturnedMLMetrics = new HashMap<>();
        for (MLMetricsItem metric : returnedMLMetrics) {
            Pair key = new ImmutablePair(metric.getComponent(), metric.getSubcomponent());
            new MiscUtil().listGetterAdder(moreReturnedMLMetrics, key, metric);  
        }
        return moreReturnedMLMetrics;
    }

    // dup
    private void addNewest(List<MLMetricsItem> mlTests, MLMetricsItem test, Double confidence) {
        if (test.getTestAccuracy() == null || test.getTestAccuracy() < confidence) {
            return;
        }
        if (test.getThreshold() == null || test.getThreshold() != 1.0) {
            return;
        }
        MLMetricsItem replace = null;
        for (MLMetricsItem aTest : mlTests) {
            Boolean moregeneralthan = aTest.moreGeneralThan(test);
            // we don't need this anymore
            if (false && moregeneralthan != null && moregeneralthan) {
                replace = aTest;
                break;
            }
            Boolean olderthan = aTest.olderThan(test);
            if (olderthan != null && olderthan) {
                replace = aTest;
                break;
            }
        }
        if (replace != null) {
            int index = mlTests.indexOf(replace);
            mlTests.set(index, test);
            return;
        }
        mlTests.add(test);
    }

    public void handleProfit(String param) {
        //param = getParam(param);
        List<String> output = new ArrayList<>();
        PipelineData data = JsonUtil.convertnostrip(param, PipelineData.class);
        // TODO
        if (data.getMap().isEmpty()) {
            
        }
        String id = PipelineUtils.getString(data, EvolveConstants.ID);
        // TODO
        List<SerialScoreChromosome> myList0 = PipelineUtils.getList(data, id);
        for (SerialScoreChromosome my : myList0) {
            //System.out.println(my.getKey() + " " + my.getRight());
        }
        List<ImmutablePair<Double, String>> myList = myList0.stream().map(e -> new ImmutablePair<Double, String>(e.getLeft(), "" + e.getRight())).collect(Collectors.toList());
        Map<String, Object> aconf = PipelineUtils.getMap(data, EvolveConstants.DEFAULT);
        //System.out.println("aconf" + aconf);
        String title = PipelineUtils.getString(data, EvolveConstants.TITLETEXT);
        //System.out.println(title);
        String[] parts = title.split(" ");
        String market = parts[1];
        String component = parts[2];
        String subcomponent = parts[3];
        String subsubcomponent = parts[4];
        //String subsubcomponent2 = parts[5];
        
        
        
        ConfigMapChromosome2 firstChromosome = (ConfigMapChromosome2) myList0.get(0).getRight();

        Map<String, Object> aMap = firstChromosome.getMap();
        aMap.remove(ConfigConstants.MACHINELEARNINGMLDYNAMIC);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLCLASSIFY);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLLEARN);
        aMap.remove(ConfigConstants.MACHINELEARNINGMLCROSS);
        aMap.remove(ConfigConstants.MISCMYTABLEDAYS);
        aMap.remove(ConfigConstants.MISCMYDAYS);
        MapDifference<String, Object> diff = Maps.difference(aMap, aconf);
        aMap = diff.entriesInCommon();
        Set<String> keys = aMap.keySet();
        //Map<String, Object> anotherMap = new HashMap<>(firstChromosome.getMap());
        //anotherMap.keySet().removeAll(keys);

        Pair<String, String> subComponent = new ImmutablePair<>(subcomponent, subsubcomponent);
        List<MLMetricsItem> mltests = null;
        try {
            mltests = dbDao.getAllMLMetrics(market, null, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<Pair<String, String>, List<MLMetricsItem>> met = getMLMetrics(mltests, null);
        //System.out.println("co" + component);
        //System.out.println("su" + subcomponent);
        //System.out.println("met" + met.keySet());
        List<MLMetricsItem> blbl = met.get(new ImmutablePair(component, subComponent.getLeft() + " " + subComponent.getRight()));
        double avg = 0;
        if (blbl != null) {
            avg = blbl.stream().map(MLMetricsItem::getTestAccuracy).mapToDouble(e -> e).average().orElse(0);
        }
        double newer = myList.get(0).getLeft();
        boolean better = avg < newer;
        myList.add(new ImmutablePair(avg, aconf + " (default)"));
        Comparator<Pair> comparator = new Comparator<>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return Double.valueOf((Double)o1.getLeft()).compareTo(Double.valueOf((Double)o2.getLeft()));
            }
        };
        Collections.sort(myList, comparator);
        Collections.reverse(myList);
        
        List<String> list2 = new ArrayList<>();
        list2 = new ArrayList<>(firstChromosome.getMap().keySet());
        list2.removeAll(keys);
        output.add("Common");
        output.add(newer + " " + aMap);
        output.add("" + list2);
        output.add("");
   
        

        
        for (SerialScoreChromosome my : myList0) {
            output.add(my.getLeft() + " " + my.getRight());
        }
        output.add("");
        output.add("Summary: " + better + " " + MathUtil.round(avg, 2) + " vs " + newer);
        String text = printtext(ServiceConstants.EVOLVEFILTERPROFIT + " " + title, "File " + id, output);
        print(text);
        if (better) {
            ConfigMapChromosome2 c = (ConfigMapChromosome2) myList0.get(0).getRight();
            ConfigMapGene conf2 = c.getGene();
            saveBetter(market, component, subComponent, IclijConfigConstants.FINDPROFIT, myList.get(0).getLeft(), IclijConstants.ALL, conf2.getMap(), true, IclijConfigConstants.MACHINELEARNING);
            /*
            try {
            	String mysubcomponent = subComponent.getLeft() + " " + subComponent.getRight();
            	log.info("Deleting AboveBelow etc {} {} {}", market, component, mysubcomponent);
                new TimingItem().delete(market, IclijConstants.FINDPROFIT, component, mysubcomponent, null, null);
                new IncDecItem().delete(market, component, mysubcomponent, null, null);
                new MemoryItem().delete(market, component, mysubcomponent, null, null);
                new AboveBelowItem().delete(market, null, null);
                IclijConfig instance = IclijXMLConfig.getConfigInstance();
                send(ServiceConstants.POPULATE, new String[] { market, component, mysubcomponent }, instance);            
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            */
        }
    }

    public void handleFilter(String param) {
        //param = getParam(param);
        //Map<String, Object> myMap = convert(param, new TypeReference<List<LinkedHashMap<Double, MarketFilterChromosome2>>>(){});
        PipelineData data = JsonUtil.convertnostrip(param, PipelineData.class);
        String id = PipelineUtils.getString(data, EvolveConstants.ID);
        // TODO
        List<SerialScoreChromosome> myList = PipelineUtils.getList(data, id);
        //List<SerialScoreChromosome> myList = (List<SerialScoreChromosome>) myMap.get(id);
        //List myList0 = PipelineUtils.getListPlain(data, id);
        //System.out.println("myl"+ myList0.size() + " " + myList0.get(0).getClass().getCanonicalName());
        //System.out.println("myl"+ myList0.get(0));
   }

    public void handleAboveBelow(String param) {
        //param = getParam(param);
        List<String> output = new ArrayList<>();
        PipelineData data = JsonUtil.convertnostrip(param, PipelineData.class);
        //Map<String, Object> myMap = new HashMap<>(); //convert(param, new TypeReference<List<LinkedHashMap<Double, AboveBelowChromosome>>>(){});
        String id = PipelineUtils.getString(data, EvolveConstants.ID);
        // TODO
        List<SerialScoreChromosome> myList = PipelineUtils.getList(data, id);
        List myList0 = PipelineUtils.getList(data, id);
        System.out.println("myl"+ myList0.size() + " " + myList0.get(0).getClass().getCanonicalName());
        String title = PipelineUtils.getString(data, EvolveConstants.TITLETEXT);
        String[] parts = title.split(" ");
        String market = parts[1];
        String subtitle = PipelineUtils.getString(data, EvolveConstants.SUBTITLETEXT);
        System.out.println(subtitle);
        List<List<String>> listlist = JsonUtil.convertnostrip(subtitle, List.class /* TypeReference<List<List<String>>>(){}*/);
        List<String> components = listlist.get(0);
        List<String> subcomponents = listlist.get(1);
        //System.out.println(components);
        //System.out.println(subcomponents);
        int jj=0;
        List<String> allcomponents = new ArrayList<>(components);
        allcomponents.addAll(subcomponents);
        Map<Double, List<AbstractChromosome>> chromosomeMap = groupCommon(myList, output);
        getCommon(chromosomeMap, allcomponents, output);
        //output.add("");
        //output.add("Summary: " + better + " " + MathUtil.round(avg, 2) + " vs " + newer);
        String text = printtext(ServiceConstants.EVOLVEFILTERABOVEBELOW + " " + title, "File " + id, output);
        print(text);
        double newer = myList.get(0).getLeft();
        Double dflt = PipelineUtils.getDouble(data, EvolveConstants.DEFAULT);
        boolean better = dflt < newer;
        // for all better, find entry with minimal trues
        List<AbstractChromosome> alist = chromosomeMap.get(newer);
        List<Long> counts = new ArrayList<>();
        for (AbstractChromosome aChromosome : alist) {
            AboveBelowChromosome ab = (AboveBelowChromosome) aChromosome;
            List<Boolean> genes = ab.getGenes();
            long count = genes.stream().filter(g -> g).count();
            counts.add(count);
        }
        int minIndex = counts.indexOf(Collections.min(counts));
        List<Boolean> genes = ((AboveBelowChromosome) myList.get(minIndex).getRight()).getGenes();
        if (better) {
            // duplicated
            List<String> mycomponents = new ArrayList<>();
            List<String> mysubcomponents = new ArrayList<>();
            int size1 = components.size();
            int size2 = subcomponents.size();
            for (int i2 = 0; i2 < size1; i2++) {
                Boolean b = genes.get(i2);
                if (b) {
                String component = components.get(i2);
                mycomponents.add(component);
                }
            }
            for (int i1 = 0; i1 < size2; i1++) {
                Boolean b = genes.get(size1 + i1);
                if (b) {
                String subcomponent = subcomponents.get(i1);
                mysubcomponents.add(subcomponent);
                }
            }
            AboveBelowItem abovebelow = new AboveBelowItem();
            abovebelow.setComponents(JsonUtil.convert(mycomponents));
            String date = PipelineUtils.getString(data, EvolveConstants.DATE);
            Date date2 = null;;
            try {
                date2 = TimeUtil.convertDate2(date);
            } catch (ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            abovebelow.setDate(date2);
            abovebelow.setMarket(market);
            abovebelow.setRecord(LocalDate.now());
            abovebelow.setScore(newer);
            abovebelow.setSubcomponents(JsonUtil.convert(mysubcomponents));
            try {
                dbDao.save(abovebelow);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    private Map<String, Object> convert(String param, TypeReference typeref) {
        Map<String, Object> map = new HashMap<>();
        //Map<String, Object> map = JsonUtil.convert(param, Map.class);
        //map.keySet();
        List<SerialScoreChromosome> myList = new ArrayList<>();
        //List<Map<Double, AbstractChromosome>> res = null;
        Map<String, Object /*List<LinkedHashMap<Double, NeuralNetChromosome2>>*/> res0 = null;
        try {
            //res = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            //res0 = mapper.readValue(param, new TypeReference<Map<String, List<LinkedHashMap<Double, NeuralNetChromosome2>>>>(){});
            res0 = JsonUtil.convertnostrip(param, new TypeReference<Map<String, Object>>(){}, mapper);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (res0 == null) {
            return map;
        }
        res0 = (Map<String, Object>) res0.get("map");
        for (Entry<String, Object> entry : res0.entrySet()) {
            //System.out.println(entry.getKey() + " " + entry.getValue().getClass().getName());
            String key = entry.getKey();
            Object value = entry.getValue();
            //System.out.println(value.getClass().getName());
            if (value == null) {
                continue;
            }
            if (value.getClass() == String.class || value.getClass() == Double.class) {
                //map.put(key, value);
                //System.out.println("kv"+key+value);
            } else if (value.getClass() == LinkedHashMap.class) {
                TypeReference typeref2 = new TypeReference<Map<String, Object>>(){};
                value = mapper.convertValue(value, typeref2);
            } else {
                //Class myclass = chromosomeClass;
                //System.out.println("xxx" + chromosomeClass.getName());
                //String s = ((List<LinkedHashMap<String, T>>)value).get(0).keySet().iterator().next();
                //System.out.println("yyy" + ((List<LinkedHashMap<String, T>>)value).get(0).get(s).getClass().getName());
                //System.out.println("val"+ value);
                value = mapper.convertValue(value, typeref);
                //Double d = ((List<LinkedHashMap<Double, T>>)value).get(0).keySet().iterator().next();
                //System.out.println("zzz" + ((List<LinkedHashMap<Double, T>>)value).get(0).get(d).getClass().getName());
            }
            map.put(key, value);
        }
        //Object bl = res0.get("1611321055358.txt");
        //List l = (List) bl;
        //System.out.println(l.get(0).getClass().getName());
        String id = (String) map.get(EvolveConstants.ID);
        List<LinkedHashMap<Double, AbstractChromosome>> res = (List<LinkedHashMap<Double, AbstractChromosome>>) map.get(id);
        //for (Map<Double, AbstractChromosome> map : res) {
        for (LinkedHashMap<Double, AbstractChromosome> aMap : res) {
            //for (Entry<Double, AbstractChromosome> entry : map.entrySet()) {
            for (Entry<Double, AbstractChromosome> entry : aMap.entrySet()) {
                Double score = entry.getKey();
                //System.out.println(entry.getValue());
                AbstractChromosome chromosome = (AbstractChromosome) entry.getValue();
                // do this or don't do strip in jsonutil
                if (chromosome instanceof ConfigMapChromosome2) {
                	ConfigMapChromosome2 chromo = (ConfigMapChromosome2) chromosome;
                	Map<String, Object> map2 = chromo.getGene().getMap();
                	if (map2.containsKey(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST)) {
                		Extra[] extras = JsonUtil.convert((String)map2.get(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST), Extra[].class);
                		map2.put(ConfigConstants.AGGREGATORSINDICATOREXTRASLIST, extras);              	
                	}
                }
                SerialScoreChromosome pair = new SerialScoreChromosome(score, chromosome);
                myList.add(pair);
            }
        }
       /*
        List<LinkedHashMap> list = JsonUtil.convert(param, List.class);
        for (LinkedHashMap<Double, LinkedHashMap> map : list) {
            for (Entry<Double, LinkedHashMap> entry : map.entrySet()) {
                LinkedHashMap<String, LinkedHashMap> map2 = entry.getValue();
                for (Entry<String, LinkedHashMap> entry2 : map2.entrySet()) {
                    LinkedHashMap<Integer, LinkedHashMap> map3 = entry2.getValue();
                    for (Entry<Integer, LinkedHashMap> entry3 : map3.entrySet()) {
                        LinkedHashMap<String, Object> map4 = entry3.getValue();
                        for (Entry<String, Object> entry4 : map4.entrySet()) {
                            System.out.println(entry4);
                        }
                    }
                }
            }
        }
        */
        map.put(id, myList);
        return map;
    }

    public String print(String title, String subtitle, List<String> individuals) {
        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(title + "\n\n");
            if (subtitle != null) {
                writer.write(subtitle + "\n\n");
            }
            for (String individual : individuals) {
                writer.write(individual + "\n");            
            }
            writer.write("\n");
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return path.getFileName().toString();
    }
    
    public String printtext(String title, String subtitle, List<String> individuals) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title + "\n\n");
        if (subtitle != null) {
            stringBuilder.append(subtitle + "\n\n");
        }
        for (String individual : individuals) {
            stringBuilder.append(individual + "\n");            
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
    
    private Map<Double, List<AbstractChromosome>> groupCommon(List<SerialScoreChromosome> myList, List<String> output) {
        List<Pair<Double, List<AbstractChromosome>>> retlist = new ArrayList<>();
        Map<Double, List<AbstractChromosome>> chromosomeMap = new LinkedHashMap<>();
        for (SerialScoreChromosome aPair : myList) {
            new MiscUtil().listGetterAdder(chromosomeMap, aPair.getLeft(), aPair.getRight());
        }
        return chromosomeMap;
    }

    private void getCommon(Map<Double, List<AbstractChromosome>> chromosomeMap, List<String> allcomponents, List<String> output) {
        for (Entry<Double, List<AbstractChromosome>> entry : chromosomeMap.entrySet()) {
            Double score = entry.getKey();
            List<AbstractChromosome> aList = entry.getValue();
            if (aList.size() == 1) {
                continue;
            }
            AboveBelowChromosome firstChromosome = (AboveBelowChromosome) aList.get(0);
            Map<String, Object> aMap = convert(allcomponents, firstChromosome.getGenes());
            for (int i = 1; i < aList.size(); i++) {
                AboveBelowChromosome anotherChromosome = (AboveBelowChromosome) aList.get(i);
                Map<String, Object> anotherMap = convert(allcomponents, anotherChromosome.getGenes());
                MapDifference<String, Object> diff = Maps.difference(aMap, anotherMap);
                aMap = diff.entriesInCommon();
            }
            Set<String> keys = aMap.keySet();
            List<Map<String, Object>> list = new ArrayList<>();
            for (AbstractChromosome chromosome : aList) {
                AboveBelowChromosome anotherChromosome = (AboveBelowChromosome) chromosome;
                Map<String, Object> anotherMap = new HashMap<>(convert(allcomponents, anotherChromosome.getGenes()));
                anotherMap.keySet().removeAll(keys);
                list.add(anotherMap);
            }
            List<String> list2 = new ArrayList<>();
            list2 = new ArrayList<>(allcomponents);
            list2.removeAll(keys);
            output.add("Common");
            output.add(entry.getKey() + " " + aList.size() + " " + aMap);
            output.add("" + list2);
            output.add("");
        }
    }

    private Map<String, Object> convert(List<String> allcomponents, List<Boolean> genes) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < allcomponents.size(); i++) {
            map.put(allcomponents.get(i), genes.get(i));
        }
        return map;
    }

    private String getParam(String param) {
        InmemoryMessage message = JsonUtil.convert(param, InmemoryMessage.class);
        Inmemory inmemory = InmemoryFactory.get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String newparam = inmemory.read(message);
        inmemory.delete(message);
        return newparam;
    }

    public void send(String service, Object object, ObjectMapper objectMapper) {
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        Communication c = CommunicationFactory.get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, null);
        c.send(object);
    }

    public void send(String service, Object object, IclijConfig config) {
        Inmemory inmemory = InmemoryFactory.get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
        String id = service + System.currentTimeMillis() + UUID.randomUUID();
        InmemoryMessage message = inmemory.send(id, object);
        send(service, message);
    }

    public void send(String service, Object object) {
        if (object == null) {
            log.error("Empty msg for {}", service);
            return;
        }
        send(service, object, mapper);
    }

    public void print(String text) {
        String node = iclijConfig.getEvolveSaveLocation();
        String mypath = iclijConfig.getEvolveSavePath();
        configCurator(iclijConfig);
        fileSystemDao.writeFile(node, mypath, null, text);
    }

    public static void configCurator(IclijConfig conf) {
        if (true) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);        
            String zookeeperConnectionString = conf.getZookeeper();
            if (curatorClient == null) {
                curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
                curatorClient.start();
            }
        }
    }

}
