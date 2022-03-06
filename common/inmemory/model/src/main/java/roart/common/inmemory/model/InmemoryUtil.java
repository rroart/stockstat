package roart.common.inmemory.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

import org.apache.commons.codec.digest.DigestUtils;

public class InmemoryUtil {
    private static Logger log = LoggerFactory.getLogger(InmemoryUtil.class);

    public static boolean validate(String md5, String content) {
        String contentMd5 = DigestUtils.md5Hex(content.getBytes(StandardCharsets.ISO_8859_1) );
        if (!md5.equals(contentMd5)) {
            log.error("Md5 differs {} {}", md5, contentMd5);
            return false;
        }
        return true;
    }
    
    public static boolean validate(String md5, InputStream content) {
        String contentMd5;
        try {
            contentMd5 = DigestUtils.md5Hex(content);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }
        if (!md5.equals(contentMd5)) {
            log.error("Md5 differs {} {}", md5, contentMd5);
            return false;
        }
        return true;
    }
    
    public static byte[] convertWithCharset(String content) {
        return content.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static String convertWithCharset(byte[] bytes) {
        try {
            return new String(bytes, "ISO_8859_1");
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    public static String convertWithCharset(Path path) {
        try {
            return new String(Files.readAllBytes(path), "ISO_8859_1");
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }
}
