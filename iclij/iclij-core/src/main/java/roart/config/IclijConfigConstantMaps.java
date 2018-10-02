package roart.config;

import java.util.HashMap;
import java.util.Map;
import roart.config.IclijConfigConstants;

public class IclijConfigConstantMaps {
    public static Map<String, Class> map = new HashMap<>();

    public static void makeTypeMap() {
        if (!map.isEmpty()) {
            return;
        }
        map.put(IclijConfigConstants.AUTORUN, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEML, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLDNN, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLDNNL, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLL, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLLR, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLMCP, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLOVR, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, String.class);
        map.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.MPSERVERCPU, Double.class);
        map.put(IclijConfigConstants.MPCLIENTCPU, Double.class);
        map.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERMLMACD, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERPREDICTOR, Boolean.class);
        map.put(IclijConfigConstants.SINGLEMARKETLOOPS, Integer.class);
        map.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, Integer.class);
        map.put(IclijConfigConstants.VERIFICATION, Boolean.class);
        map.put(IclijConfigConstants.VERIFICATIONSAVE, Boolean.class);
        map.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONDAYS, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONLOOPS, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, Boolean.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKET, String.class);
        map.put(IclijConfigConstants.MARKETSTRADEMARKETSMARKET, String.class);
        map.put(IclijConfigConstants.MARKETSIMPORTANTSIMPORTANT, String.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKET2, String.class);
        map.put(IclijConfigConstants.MARKETSTRADEMARKETSMARKET2, String.class);
        map.put(IclijConfigConstants.MARKETSIMPORTANTSIMPORTANT2, String.class);
    }

    public static Map<String, Object> deflt = new HashMap<>();
    public static void makeDefaultMap() {
        if (!deflt.isEmpty()) {
            return;
        }
        deflt.put(IclijConfigConstants.AUTORUN, Boolean.FALSE);
        deflt.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEML, Boolean.FALSE);
        deflt.put(IclijConfigConstants.EVOLVEMLDNN, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEMLDNNL, Boolean.FALSE);
        deflt.put(IclijConfigConstants.EVOLVEMLL, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEMLLR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEMLMCP, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEMLOVR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, "{ 'generations' : 100, 'children' : 4, 'crossover' : 2, 'elite' : 1, 'elitecloneandmutate' : 1, 'select' : 16, 'mutate' : 2, 'generationcreate' : 1, 'useoldelite' : true }");
        deflt.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, "{ 'generations' : 10, 'children' : 4, 'crossover' : 2, 'elite' : 1, 'elitecloneandmutate' : 1, 'select' : 4, 'mutate' : 2, 'generationcreate' : 1, 'useoldelite' : true }");
        deflt.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, "http://localhost:8000");
        deflt.put(IclijConfigConstants.MPSERVERCPU, 0.5);
        deflt.put(IclijConfigConstants.MPCLIENTCPU, 0.5);
        deflt.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERMLMACD, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERPREDICTOR, Boolean.FALSE);
        deflt.put(IclijConfigConstants.SINGLEMARKETLOOPS, 5);
        deflt.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, 20);
        deflt.put(IclijConfigConstants.VERIFICATION, Boolean.TRUE);
        deflt.put(IclijConfigConstants.VERIFICATIONSAVE, Boolean.FALSE);
        deflt.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, 10);
        deflt.put(IclijConfigConstants.VERIFICATIONDAYS, 20);
        deflt.put(IclijConfigConstants.VERIFICATIONLOOPS, 5);
        deflt.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, 20);
        deflt.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, Boolean.FALSE);
    }

    public static Map<String, String> text = new HashMap<>();

    public static void makeTextMap() {
        if (!text.isEmpty()) {
            return;
        }
        text.put(IclijConfigConstants.AUTORUN, "Enable autorun");
        text.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, "Enable evolution generated recommender settings");
        text.put(IclijConfigConstants.EVOLVEML, "Enable evolution generated ml configs");
        text.put(IclijConfigConstants.EVOLVEMLDNN, "Enable evolution generated ml DNNconfigs");
        text.put(IclijConfigConstants.EVOLVEMLDNNL, "Enable evolution generated ml DNNLconfigs");
        text.put(IclijConfigConstants.EVOLVEMLL, "Enable evolution generated ml L configs");
        text.put(IclijConfigConstants.EVOLVEMLLR, "Enable evolution generated ml LR configs");
        text.put(IclijConfigConstants.EVOLVEMLMCP, "Enable evolution generated ml MCP configs");
        text.put(IclijConfigConstants.EVOLVEMLOVR, "Enable evolution generated ml OVR configs");
        text.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, "Enable evolution generated ml server");
        text.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, "ML GP config");
        text.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, "Indicator recommender GP config");
        text.put(IclijConfigConstants.MPSERVERCPU, "Server cpu usage");
        text.put(IclijConfigConstants.MPCLIENTCPU, "Client cpu usage");
        text.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, "Enable recommender mlindicator");
        text.put(IclijConfigConstants.RECOMMENDERMLMACD, "Enable recommender mlmacd");
        text.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, "Enable recommender evolution based");
        text.put(IclijConfigConstants.RECOMMENDERPREDICTOR, "Enable recommender predictor");
        text.put(IclijConfigConstants.SINGLEMARKETLOOPS, "Single market loops");
        text.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, "Single market loop interval");
        text.put(IclijConfigConstants.VERIFICATION, "Enable verification");
        text.put(IclijConfigConstants.VERIFICATIONSAVE, "Enable verification save");
        text.put(IclijConfigConstants.VERIFICATIONDAYS, "Verification days");
        text.put(IclijConfigConstants.VERIFICATIONLOOPS, "Verification loops");
        text.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, "Verification loop interval");
        text.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, "Number of entries to pick from recommender lists");
        text.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, "Enable improve profit");
    }

    public static Map<String, String> conv = new HashMap<>();
    public static void makeConvertMap() {
        if (!conv.isEmpty()) {
            return;
        }
        conv.put(IclijConfigConstants.EVOLVEMLDNN, ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        conv.put(IclijConfigConstants.EVOLVEMLDNNL, ConfigConstants.MACHINELEARNINGTENSORFLOWDNNL);
        conv.put(IclijConfigConstants.EVOLVEMLL, ConfigConstants.MACHINELEARNINGTENSORFLOWL);
        conv.put(IclijConfigConstants.EVOLVEMLLR, ConfigConstants.MACHINELEARNINGSPARKMLLR);
        conv.put(IclijConfigConstants.EVOLVEMLMCP, ConfigConstants.MACHINELEARNINGSPARKMLMCP);
        conv.put(IclijConfigConstants.EVOLVEMLOVR, ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        conv.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER);
        conv.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG);
        conv.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, ConfigConstants.EVOLVEMLEVOLUTIONCONFIG);
    }
    
    public static Map<String, IclijXMLType> mymap = new HashMap<>();
    public static void makeMap() {
        mymap.put(IclijConfigConstants.AUTORUN, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable autorun"));
        mymap.put(IclijConfigConstants.EVOLVE, new IclijXMLType(null, null, null));
        mymap.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated recommender settings"));
        mymap.put(IclijConfigConstants.EVOLVEML, new IclijXMLType(Boolean.class, Boolean.FALSE, "Evolve ML enable"));
        mymap.put(IclijConfigConstants.EVOLVEMLDNN, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated ml DNNconfigs", ConfigConstants.MACHINELEARNINGTENSORFLOWDNN));
        mymap.put(IclijConfigConstants.EVOLVEMLDNNL, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable evolution generated ml DNNLconfigs", ConfigConstants.MACHINELEARNINGTENSORFLOWDNNL));
        mymap.put(IclijConfigConstants.EVOLVEMLL, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated ml L configs", ConfigConstants.MACHINELEARNINGTENSORFLOWL));
        mymap.put(IclijConfigConstants.EVOLVEMLLR, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated ml LR configs", ConfigConstants.MACHINELEARNINGSPARKMLLR));
        mymap.put(IclijConfigConstants.EVOLVEMLMCP, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated ml MCP configs", ConfigConstants.MACHINELEARNINGSPARKMLMCP));
        mymap.put(IclijConfigConstants.EVOLVEMLOVR, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated ml OVR configs", ConfigConstants.MACHINELEARNINGSPARKMLOVR));
        mymap.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, new IclijXMLType(String.class, "http://localhost:8000", "Enable evolution generated ml server", ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER));
        mymap.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, new IclijXMLType( String.class, "{ 'generations' : 100, 'children' : 4, 'crossover' : 2, 'elite' : 1, 'elitecloneandmutate' : 1, 'select' : 16, 'mutate' : 2, 'generationcreate' : 1 }", "ML GP config", ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG));
        mymap.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, new IclijXMLType( String.class, "{ 'generations' : 10, 'children' : 4, 'crossover' : 2, 'elite' : 1, 'elitecloneandmutate' : 1, 'select' : 4, 'mutate' : 2, 'generationcreate' : 1 }", "Indicator recommender GP config", ConfigConstants.EVOLVEMLEVOLUTIONCONFIG));
        mymap.put(IclijConfigConstants.MPSERVERCPU, new IclijXMLType(Double.class, 0.5, "Server cpu usage"));
        mymap.put(IclijConfigConstants.MPCLIENTCPU, new IclijXMLType(Double.class, 0.5, "Client cpu usage"));
        mymap.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable recommender mlindicator"));
        mymap.put(IclijConfigConstants.RECOMMENDERMLMACD, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable recommender mlmacd"));
        mymap.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable recommender evolution based"));
        mymap.put(IclijConfigConstants.RECOMMENDERPREDICTOR, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable recommender predictor"));
        mymap.put(IclijConfigConstants.SINGLEMARKETLOOPS, new IclijXMLType(Integer.class, 5, "Single market loops"));
        mymap.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, new IclijXMLType(Integer.class, 20, "Single market loop interval"));
        mymap.put(IclijConfigConstants.VERIFICATION, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable verification"));
        mymap.put(IclijConfigConstants.VERIFICATIONSAVE, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable verification save"));
        mymap.put(IclijConfigConstants.VERIFICATIONDAYS, new IclijXMLType(Integer.class, 20, "Verification days"));
        mymap.put(IclijConfigConstants.VERIFICATIONLOOPS, new IclijXMLType(Integer.class, 5, "Verification loops"));
        mymap.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, new IclijXMLType(Integer.class, 20, "Verification loop interval"));
        mymap.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, new IclijXMLType(Integer.class, 10, "Number of entries to pick from recommender lists"));
        mymap.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable improve profit"));
        
    }
}
