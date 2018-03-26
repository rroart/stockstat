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
        map.put(IclijConfigConstants.MPSERVERCPUFRACTION, Integer.class);
        map.put(IclijConfigConstants.MPCLIENTCPUFRACTION, Integer.class);
        map.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERMLMACD, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, Boolean.class);
        map.put(IclijConfigConstants.RECOMMENDERPREDICTOR, Boolean.class);
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
        deflt.put(IclijConfigConstants.MPSERVERCPUFRACTION, 4);
        deflt.put(IclijConfigConstants.MPCLIENTCPUFRACTION, 4);
        deflt.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERMLMACD, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, Boolean.TRUE);
        deflt.put(IclijConfigConstants.RECOMMENDERPREDICTOR, Boolean.FALSE);
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
        text.put(IclijConfigConstants.MPSERVERCPUFRACTION, "Server cpu fraction usage");
        text.put(IclijConfigConstants.MPCLIENTCPUFRACTION, "Client cpu fraction usage");
        text.put(IclijConfigConstants.RECOMMENDERMLINDICATOR, "Enable recommender mlindicator");
        text.put(IclijConfigConstants.RECOMMENDERMLMACD, "Enable recommender mlmacd");
        text.put(IclijConfigConstants.RECOMMENDERRECOMMENDER, "Enable recommender evolution based");
        text.put(IclijConfigConstants.RECOMMENDERPREDICTOR, "Enable recommender predictor");
        text.put(IclijConfigConstants.VERIFICATION, "Enable verification");
        text.put(IclijConfigConstants.VERIFICATIONSAVE, "Enable verification save");
        text.put(IclijConfigConstants.VERIFICATIONDAYS, "Verification days");
        text.put(IclijConfigConstants.VERIFICATIONLOOPS, "Verification loops");
        text.put(IclijConfigConstants.VERIFICATIONLOOPINTERVAL, "Verification loop interval");
        text.put(IclijConfigConstants.VERIFICATIONRECOMMENDERTOPBOTTOM, "Number of entries to pick from recommender lists");
        text.put(IclijConfigConstants.VERIFICATIONIMPROVEPROFIT, "Enable improve profit");
    }

}
