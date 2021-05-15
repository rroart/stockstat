package roart.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import roart.common.constants.Constants;

public class JsonUtil {

    private static Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public static <T> T convert(String text, TypeReference<T> typeref) {
        ObjectMapper mapper = new ObjectMapper();
        if (text != null) {
            try {
                String strippedtext = strip(text);
                return mapper.convertValue(strippedtext, typeref);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static <T> T convertnostrip(String text, Class<T> myclass) {
        ObjectMapper mapper = new ObjectMapper();
        if (text != null) {
            try {
                return mapper.readValue(text, myclass);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static <T> T convert(String text, Class<T> myclass) {
        ObjectMapper mapper = new ObjectMapper();
        if (text != null) {
            try {
                String strippedtext = strip(text);
                return mapper.readValue(strippedtext, myclass);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static <T> T convert(String text, Class<T> myclass, ObjectMapper mapper) {
        if (text != null) {
            try {
                String strippedtext = strip(text);
                return mapper.readValue(strippedtext, myclass);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static String strip(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String strippedtext = text;
        if (strippedtext.charAt(0) == '\"') {
            strippedtext = strippedtext.substring(1, strippedtext.length() - 1);
        }
        strippedtext = strippedtext.replaceAll("\\\\", "");
        if (text.length() != strippedtext.length()) {
            log.info("Stripping json text {} to {}", text, strippedtext);
        }
        return strippedtext;
    }

    public static String convert(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        if (object != null) {
            try {
                return mapper.writeValueAsString(object);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static String convert(Object object, ObjectMapper mapper) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        if (object != null) {
            try {
                return mapper.writeValueAsString(object);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }

    public static <T> T copy(Object object) {
        String text = convert(object);
        return (T) convert(text, object.getClass());
    }
}
