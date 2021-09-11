/**
 * 
 */
/**
 * @author roart
 *
 */
module common.webflux {
    exports roart.common.webflux;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires spring.core;
    requires spring.web;
    requires spring.webflux;
    requires common.constants;
    requires common.util;
    requires reactor.core;
}
