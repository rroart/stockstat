package roart.common.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateUtil {
    private static Logger log = LoggerFactory.getLogger(ValidateUtil.class);

    private static boolean done = false;
    
    public static void validateSizes(List oneList, List secondList) {
        if (oneList != null && secondList != null && oneList.size() != secondList.size()) {
            log.error("Size mismatch {} vs {}", oneList.size(), secondList.size());
            if (!done) {
                log.error("" + oneList);
                log.error("" + secondList);
                done = true;
                if (true) {
                    try {
                        String s = null;
                        s.length();
                    } catch (Exception e) {
                        log.error("EXCEPTION", e);
                    }
                }
            }
        }
    }
}
