package roart.common.util;

public class MiscUtil {
    public static String getSubComponent(String mlname, String modelname) {
        return mlname + " " + modelname;
    }
    
    public static String getLocalComponent(String subtype, String subsubtype) {
        String localcomponent = null;
        if (subtype != null) {
            localcomponent = subtype + subsubtype;
        }
        return localcomponent;
    }
}