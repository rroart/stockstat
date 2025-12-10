/**
 * 
 */
/**
 * @author roart
 *
 */
module common.eureka {
    exports roart.eureka.util;

    requires tools.jackson.core;
    requires tools.jackson.databind;
    requires org.slf4j;
    requires spring.core;
    requires spring.web;
    requires common.constants;
    requires common.util;
}
