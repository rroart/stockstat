package roart.util;

import javax.persistence.Transient;

import roart.model.Meta;

public class MetaDao {
    @Transient
    public static String getPeriod(Meta meta, int i) throws Exception {
        if (i == 1) {
            return meta.getPeriod1();
        }
        if (i == 2) {
            return meta.getPeriod2();
        }
        if (i == 3) {
            return meta.getPeriod3();
        }
        if (i == 4) {
            return meta.getPeriod4();
        }
        if (i == 5) {
            return meta.getPeriod5();
        }
        if (i == 6) {
            return meta.getPeriod6();
        }
        throw new Exception("Out of range " + i);
    }
}
