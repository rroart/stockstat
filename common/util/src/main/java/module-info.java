/**
 * 
 */
/**
 * @author roart
 *
 */
module common.util {
    exports roart.common.util;
    opens roart.common.util;
    requires org.slf4j;
    requires commons.math3;
    requires org.apache.commons.lang3;
    requires common.constants;
    requires common.model;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires guava;
    requires java.sql;
    requires com.fasterxml.jackson.annotation;
    //requires org.slf4j;
}
