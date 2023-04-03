/**
 * 
 */
/**
 * @author roart
 *
 */
module common.controller {
    exports roart.common.controller;

    requires common.communication.factory;
    requires common.communication.model;
    requires common.constants;
    requires common.util;
    requires org.slf4j;
    requires guava;
    requires com.fasterxml.jackson.databind;
    requires dbdao;
}
