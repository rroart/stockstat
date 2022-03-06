package roart.common.util;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

public class IOUtil {
    private static Logger log = LoggerFactory.getLogger(IOUtil.class);

    public static byte[] toByteArrayMax(InputStream is) {
        try {
            return is.readNBytes(Integer.MAX_VALUE - 8);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new byte[0];
        }
    }

    public static byte[] toByteArray1G(InputStream is) {
        try {
            return is.readNBytes(1024 * 1024 * 1024);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new byte[0];
        }
    }

    public static byte[] toByteArray(InputStream is, int bytes) {
        try {
            return is.readNBytes(bytes);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new byte[0];
        }
    }
}
