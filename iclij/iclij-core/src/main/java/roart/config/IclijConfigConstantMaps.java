package roart.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLType;

public class IclijConfigConstantMaps {
    private static final String MLCONFIG = "{ \"mcp\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true }, \"lr\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true }, \"ovr\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true }, \"dnn\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true },  \"l\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true }, \"lstm\" : { \"load\" : true, \"save\" : true, \"enable\" : false, \"evolve\" : false } }";

    private static final String MLCONFIGLSTM = "{ \"lstm\" : { \"load\" : true, \"save\" : true, \"enable\" : true, \"evolve\" : true } }";

    private static final String RECOMMENDEVOLUTIONCONFIG = "{ \"generations\" : 100, \"children\" : 4, \"crossover\" : 2, \"elite\" : 1, \"elitecloneandmutate\" : 1, \"select\" : 16, \"mutate\" : 2, \"generationcreate\" : 1, \"useoldelite\" : true }";

    private static final String MLEVOLUTIONCONFIG = "{ \"generations\" : 10, \"children\" : 4, \"crossover\" : 2, \"elite\" : 1, \"elitecloneandmutate\" : 1, \"select\" : 4, \"mutate\" : 2, \"generationcreate\" : 1, \"useoldelite\" : true }";
    
    public static Map<String, Class> map = new HashMap<>();

    public static void makeTypeMap() {
        if (!map.isEmpty()) {
            return;
        }
        map.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEML, Boolean.class);
        map.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.EVOLVEMLMLCONFIG, String.class);
        map.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, String.class);
        map.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.MISCSHUTDOWNHOUR, Integer.class);
        map.put(IclijConfigConstants.MPSERVERCPU, Double.class);
        map.put(IclijConfigConstants.MPCLIENTCPU, Double.class);
        map.put(IclijConfigConstants.FINDPROFITAUTORUN, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITMLINDICATOR, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITMLINDICATOREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLMACD, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLRSI, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITMLRSIEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLRSIMLCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLMULTI, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITMLMULTIEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITMLMULTIMLCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITPREDICTOR, Boolean.class);
        map.put(IclijConfigConstants.FINDPROFITPREDICTOREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.FINDPROFITPREDICTORMLCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITAUTORUN, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITFITNESSMINIMUM, Integer.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOR, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMACD, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLRSI, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLRSIEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLRSIMLCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMULTI, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMULTIEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITMLMULTIMLCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITPREDICTOR, Boolean.class);
        map.put(IclijConfigConstants.IMPROVEPROFITPREDICTOREVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.IMPROVEPROFITPREDICTORMLCONFIG, String.class);
        map.put(IclijConfigConstants.SINGLEMARKETEVOLVEFIRSTONLY, Boolean.class);
        map.put(IclijConfigConstants.SINGLEMARKETLOOPS, Integer.class);
        map.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, Integer.class);
        map.put(IclijConfigConstants.VERIFICATION, Boolean.class);
        map.put(IclijConfigConstants.VERIFICATIONSAVE, Boolean.class);
        map.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONDAYS, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONEVOLVEFIRSTONLY, Boolean.class);
        map.put(IclijConfigConstants.VERIFICATIONLOOPS, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, Integer.class);
        map.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, Boolean.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKET, String.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKETCONFIG, String.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKETFILTER, String.class);
        //map.put(IclijConfigConstants.MARKETSMARKETLISTMARKETEVOLUTIONCONFIG, String.class);
        map.put(IclijConfigConstants.MARKETSMARKETLISTMARKETMLCONFIG, String.class);
        map.put(IclijConfigConstants.MARKETSFILTERMARKETSMARKET, String.class);
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
        deflt.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.EVOLVEML, Boolean.FALSE);
        deflt.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, RECOMMENDEVOLUTIONCONFIG);
        deflt.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, MLEVOLUTIONCONFIG);
        deflt.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, "http://localhost:8000");
        deflt.put(IclijConfigConstants.MPSERVERCPU, 0.5);
        deflt.put(IclijConfigConstants.MPCLIENTCPU, 0.5);
        deflt.put(IclijConfigConstants.FINDPROFITAUTORUN, Boolean.FALSE);
        deflt.put(IclijConfigConstants.FINDPROFITMLINDICATOR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.FINDPROFITMLINDICATOREVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.FINDPROFITMLMACD, Boolean.TRUE);
        deflt.put(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.FINDPROFITMLRSI, Boolean.TRUE);
        deflt.put(IclijConfigConstants.FINDPROFITMLRSIEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.FINDPROFITMLRSIMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.FINDPROFITMLMULTI, Boolean.TRUE);
        deflt.put(IclijConfigConstants.FINDPROFITMLMULTIEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.FINDPROFITMLMULTIMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.FINDPROFITRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.FINDPROFITPREDICTOR, Boolean.FALSE);
        deflt.put(IclijConfigConstants.FINDPROFITPREDICTOREVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.FINDPROFITPREDICTORMLCONFIG, MLCONFIGLSTM);
        deflt.put(IclijConfigConstants.IMPROVEPROFITAUTORUN, Boolean.FALSE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITFITNESSMINIMUM, 10);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOR, Boolean.TRUE);
	deflt.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOREVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMACD, Boolean.TRUE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLRSI, Boolean.TRUE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLRSIEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLRSIMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMULTI, Boolean.TRUE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMULTIEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.IMPROVEPROFITMLMULTIMLCONFIG, MLCONFIG);
        deflt.put(IclijConfigConstants.IMPROVEPROFITRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITPREDICTOR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.IMPROVEPROFITPREDICTOREVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.IMPROVEPROFITPREDICTORMLCONFIG, MLCONFIGLSTM);
        deflt.put(IclijConfigConstants.SINGLEMARKETEVOLVEFIRSTONLY, Boolean.TRUE);
        deflt.put(IclijConfigConstants.SINGLEMARKETLOOPS, 5);
        deflt.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, 20);
        deflt.put(IclijConfigConstants.VERIFICATION, Boolean.TRUE);
        deflt.put(IclijConfigConstants.VERIFICATIONSAVE, Boolean.FALSE);
        deflt.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, 10);
        deflt.put(IclijConfigConstants.VERIFICATIONDAYS, 20);
        deflt.put(IclijConfigConstants.VERIFICATIONEVOLVEFIRSTONLY, Boolean.TRUE);
        deflt.put(IclijConfigConstants.VERIFICATIONLOOPS, 5);
        deflt.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, 20);
        deflt.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, Boolean.FALSE);
        //deflt.put(IclijConfigConstants.MARKETSMARKETLISTMARKETEVOLUTIONCONFIG, null);
        deflt.put(IclijConfigConstants.MARKETSMARKETLISTMARKETMLCONFIG, MLCONFIG);
    }

    public static Map<String, String> text = new HashMap<>();

    public static void makeTextMap() {
        if (!text.isEmpty()) {
            return;
        }
        text.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, "Enable evolution generated recommender settings");
        text.put(IclijConfigConstants.EVOLVEML, "Enable evolution generated ml configs");
        text.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, "ML GP config");
        text.put(IclijConfigConstants.EVOLVEMLMLCONFIG, "Evolution default ml enabled");
        text.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, "Enable evolution generated ml server");
        text.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, "Indicator recommender GP config");
        text.put(IclijConfigConstants.MISCSHUTDOWNHOUR, "Server shutdown hour");
        text.put(IclijConfigConstants.MPSERVERCPU, "Server cpu usage");
        text.put(IclijConfigConstants.MPCLIENTCPU, "Client cpu usage");
        text.put(IclijConfigConstants.FINDPROFITAUTORUN, "Enable find profit autorun");
        text.put(IclijConfigConstants.FINDPROFITMLINDICATOR, "Enable find profit mlindicator");
        text.put(IclijConfigConstants.FINDPROFITMLINDICATOREVOLUTIONCONFIG, "Find profit mlindicator evolution config");
        text.put(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG, "Find profit mlindicator ml enabled");
        text.put(IclijConfigConstants.FINDPROFITMLMACD, "Enable find profit mlmacd");
        text.put(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG, "Find profit mlmacd evolution config");
        text.put(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG, "Find profit mlmacd ml enabled");
        text.put(IclijConfigConstants.FINDPROFITMLRSI, "Enable find profit mlrsi");
        text.put(IclijConfigConstants.FINDPROFITMLRSIEVOLUTIONCONFIG, "Find profit mlrsi evolution config");
        text.put(IclijConfigConstants.FINDPROFITMLRSIMLCONFIG, "Find profit mlrsi ml enabled");
        text.put(IclijConfigConstants.FINDPROFITMLMULTI, "Enable find profit mlmulti");
        text.put(IclijConfigConstants.FINDPROFITMLMULTIEVOLUTIONCONFIG, "Find profit mlmulti evolution config");
        text.put(IclijConfigConstants.FINDPROFITMLMULTIMLCONFIG, "Find profit mlmulti ml enabled");
        text.put(IclijConfigConstants.FINDPROFITRECOMMENDER, "Enable find profit evolution based");
        text.put(IclijConfigConstants.FINDPROFITPREDICTOR, "Enable find profit predictor");
        text.put(IclijConfigConstants.FINDPROFITPREDICTOREVOLUTIONCONFIG, "Find profit predictor evolution config");
        text.put(IclijConfigConstants.FINDPROFITPREDICTORMLCONFIG, "Find profit predictor ml enabled");
        text.put(IclijConfigConstants.IMPROVEPROFITAUTORUN, "Enable improve profit autorun");
        text.put(IclijConfigConstants.IMPROVEPROFITFITNESSMINIMUM, "Enable improve profit fitness minimum");
        text.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOR, "Enable improve profit mlindicator");
        text.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOREVOLUTIONCONFIG, "Improve profit mlindicator evolution config");
        text.put(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG, "Improve profit mlindicator ml enabled");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMACD, "Enable improve profit mlmacd");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG, "Improve profit mlmacd evolution config");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG, "Improve profit mlmacd ml enabled");
        text.put(IclijConfigConstants.IMPROVEPROFITMLRSI, "Enable improve profit mlrsi");
        text.put(IclijConfigConstants.IMPROVEPROFITMLRSIEVOLUTIONCONFIG, "Improve profit mlrsi evolution config");
        text.put(IclijConfigConstants.IMPROVEPROFITMLRSIMLCONFIG, "Improve profit mlrsi ml enabled");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMULTI, "Enable improve profit mlmulti");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMULTIEVOLUTIONCONFIG, "Improve profit mlmulti evolution config");
        text.put(IclijConfigConstants.IMPROVEPROFITMLMULTIMLCONFIG, "Improve profit mlmulti ml enabled");
        text.put(IclijConfigConstants.IMPROVEPROFITRECOMMENDER, "Enable improve profit evolution based");
        text.put(IclijConfigConstants.IMPROVEPROFITPREDICTOR, "Enable improve profit predictor");
        text.put(IclijConfigConstants.IMPROVEPROFITPREDICTOREVOLUTIONCONFIG, "Improve profit predictor evolution config");
        text.put(IclijConfigConstants.IMPROVEPROFITPREDICTORMLCONFIG, "Improve profit predictor ml enable");
        text.put(IclijConfigConstants.SINGLEMARKETEVOLVEFIRSTONLY, "Single market loops with evolve first only");
        text.put(IclijConfigConstants.SINGLEMARKETLOOPS, "Single market loops");
        text.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, "Single market loop interval");
        text.put(IclijConfigConstants.VERIFICATION, "Enable verification");
        text.put(IclijConfigConstants.VERIFICATIONSAVE, "Enable verification save");
        text.put(IclijConfigConstants.VERIFICATIONDAYS, "Verification days");
        text.put(IclijConfigConstants.VERIFICATIONEVOLVEFIRSTONLY, "Verification loops with evolve first only");
        text.put(IclijConfigConstants.VERIFICATIONLOOPS, "Verification loops");
        text.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, "Verification loop interval");
        text.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, "Number of entries to pick from recommender lists");
        text.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, "Enable improve profit");
        //text.put(IclijConfigConstants.MARKETSMARKETLISTMARKETEVOLUTIONCONFIG, "Market ML evolution config");
        text.put(IclijConfigConstants.MARKETSMARKETLISTMARKETMLCONFIG, "Market ML config");
   }

    public static Map<String, String> conv = new HashMap<>();
    public static void makeConvertMap() {
        if (!conv.isEmpty()) {
            return;
        }
        conv.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER);
        conv.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG);
        conv.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, ConfigConstants.EVOLVEMLEVOLUTIONCONFIG);
    }
    
    public static Map<String, IclijXMLType> mymap = new HashMap<>();
    public static void makeMap() {
        mymap.put(IclijConfigConstants.EVOLVE, new IclijXMLType(null, null, null));
        mymap.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDER, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable evolution generated recommender settings"));
        mymap.put(IclijConfigConstants.EVOLVEML, new IclijXMLType(Boolean.class, Boolean.FALSE, "Evolve ML enable"));
        mymap.put(IclijConfigConstants.EVOLVEMLEVOLUTIONCONFIG, new IclijXMLType( String.class, "{ \"generations\" : 10, \"children\" : 4, \"crossover\" : 2, \"elite\" : 1, \"elitecloneandmutate\" : 1, \"select\" : 4, \"mutate\" : 2, \"generationcreate\" : 1 }", "ML GP config", ConfigConstants.EVOLVEMLEVOLUTIONCONFIG));
        mymap.put(IclijConfigConstants.EVOLVEMLMLCONFIG, new IclijXMLType(String.class, null, "Evolution default ml enabled"));
        mymap.put(IclijConfigConstants.EVOLVEMLTENSORFLOWSERVER, new IclijXMLType(String.class, "http://localhost:8000", "Enable evolution generated ml server", ConfigConstants.MACHINELEARNINGTENSORFLOWSERVER));
        mymap.put(IclijConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG, new IclijXMLType( String.class, "{ \"generations\" : 100, \"children\" : 4, \"crossover\" : 2, \"elite\" : 1, \"elitecloneandmutate\" : 1, \"select\" : 16, \"mutate\" : 2, \"generationcreate\" : 1 }", "Indicator recommender GP config", ConfigConstants.EVOLVEINDICATORRECOMMENDEREVOLUTIONCONFIG));
        mymap.put(IclijConfigConstants.MISCSHUTDOWNHOUR, new IclijXMLType(Integer.class, null, "Server shutdown hour"));
        mymap.put(IclijConfigConstants.MPSERVERCPU, new IclijXMLType(Double.class, 0.5, "Server cpu usage"));
        mymap.put(IclijConfigConstants.MPCLIENTCPU, new IclijXMLType(Double.class, 0.5, "Client cpu usage"));
        mymap.put(IclijConfigConstants.FINDPROFITAUTORUN, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable find profit autorun"));
        mymap.put(IclijConfigConstants.FINDPROFITMLINDICATOR, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable find profit mlindicator"));
        mymap.put(IclijConfigConstants.FINDPROFITMLINDICATOREVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Find profit mlindicator evolution config"));
        mymap.put(IclijConfigConstants.FINDPROFITMLINDICATORMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Find profit mlindicator ml enable"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMACD, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable find profit mlmacd"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMACDEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Find profit mlmacd evolution config"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMACDMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Find profit mlmacd ml enable"));
        mymap.put(IclijConfigConstants.FINDPROFITMLRSI, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable find profit mlrsi"));
        mymap.put(IclijConfigConstants.FINDPROFITMLRSIEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Find profit mlrsi evolution config"));
        mymap.put(IclijConfigConstants.FINDPROFITMLRSIMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Find profit mlrsi ml enable"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMULTI, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable find profit mlmulti"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMULTIEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Find profit mlmulti evolution config"));
        mymap.put(IclijConfigConstants.FINDPROFITMLMULTIMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Find profit mlmulti ml enable"));
        mymap.put(IclijConfigConstants.FINDPROFITRECOMMENDER, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable find profit evolution based"));
        mymap.put(IclijConfigConstants.FINDPROFITPREDICTOR, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable find profit predictor"));
        mymap.put(IclijConfigConstants.FINDPROFITPREDICTOREVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Find profit predictor evolution config"));
        mymap.put(IclijConfigConstants.FINDPROFITPREDICTORMLCONFIG, new IclijXMLType(String.class, MLCONFIGLSTM, "Find profit predictor ml enable"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITAUTORUN, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable improve profit autorun"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITFITNESSMINIMUM, new IclijXMLType(Integer.class, 10, "Enable improve profit fitness minimum"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOR, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable improve profit mlindicator"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLINDICATOREVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Improve profit mlindicator evolution config"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLINDICATORMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Improve profit mlindicator ml enable"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMACD, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable improve profit mlmacd"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMACDEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Improve profit mlmacd evolution config"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMACDMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Improve profit mlmacd enable"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLRSI, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable improve profit mlrsi"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLRSIEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Improve profit mlrsi evolution config"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLRSIMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Improve profit mlrsi enable"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMULTI, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable improve profit mlmulti"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMULTIEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Improve profit mlmulti evolution config"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITMLMULTIMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Improve profit mlmulti enable"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITRECOMMENDER, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable improve profit evolution based"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITPREDICTOR, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable improve profit predictor"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITPREDICTOREVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Improve profit predictor evolution config"));
        mymap.put(IclijConfigConstants.IMPROVEPROFITPREDICTORMLCONFIG, new IclijXMLType(String.class, MLCONFIGLSTM, "Improve profit predictor ml enable"));
        mymap.put(IclijConfigConstants.SINGLEMARKETEVOLVEFIRSTONLY, new IclijXMLType(Boolean.class, Boolean.TRUE, "Single market loops with evolve first only"));
        mymap.put(IclijConfigConstants.SINGLEMARKETLOOPS, new IclijXMLType(Integer.class, 5, "Single market loops"));
        mymap.put(IclijConfigConstants.SINGLEMARKETLOOPINTERVAL, new IclijXMLType(Integer.class, 20, "Single market loop interval"));
        mymap.put(IclijConfigConstants.VERIFICATION, new IclijXMLType(Boolean.class, Boolean.TRUE, "Enable verification"));
        mymap.put(IclijConfigConstants.VERIFICATIONSAVE, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable verification save"));
        mymap.put(IclijConfigConstants.VERIFICATIONDAYS, new IclijXMLType(Integer.class, 20, "Verification days"));
        mymap.put(IclijConfigConstants.VERIFICATIONEVOLVEFIRSTONLY, new IclijXMLType(Boolean.class, Boolean.TRUE, "Verification loops with evolve first only"));
        mymap.put(IclijConfigConstants.VERIFICATIONLOOPS, new IclijXMLType(Integer.class, 5, "Verification loops"));
        mymap.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, new IclijXMLType(Integer.class, 20, "Verification loop interval"));
        mymap.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, new IclijXMLType(Integer.class, 10, "Number of entries to pick from recommender lists"));
        mymap.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, new IclijXMLType(Boolean.class, Boolean.FALSE, "Enable improve profit"));
        //mymap.put(IclijConfigConstants.MARKETSMARKETLISTMARKETEVOLUTIONCONFIG, new IclijXMLType(String.class, null, "Market evolution config"));        
        mymap.put(IclijConfigConstants.MARKETSMARKETLISTMARKETMLCONFIG, new IclijXMLType(String.class, MLCONFIG, "Market ML config"));        
    }
}
