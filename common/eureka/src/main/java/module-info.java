/**
 * 
 */
/**
 * @author roart
 *
 */
module common.eureka {
    exports roart.eureka.util;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires spring.core;
    requires spring.web;
    requires common.constants;
    requires common.util;
}
