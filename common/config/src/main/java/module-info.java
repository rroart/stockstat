/**
 * 
 */
/**
 * @author roart
 *
 */
module common.config {
    exports roart.common.config;
    exports roart.common.ml;
    requires java.xml;
    requires common.util;
    requires com.fasterxml.jackson.annotation;
    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires commons.configuration2;
    requires common.constants;
    requires spring.context;
    requires org.apache.commons.lang3;
}
