/**
 * 
 */
/**
 * @author roart
 *
 */
open module model {
    exports roart.model.data;

    requires java.sql;
    requires common.constants;
    requires commons.math3;
    requires common.model;
    requires org.slf4j;
    requires common.config;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.annotation;
}
