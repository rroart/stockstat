/**
 * 
 */
/**
 * @author roart
 *
 */
open module coredb.spring {
    exports roart.db.spring;

    requires iclij.common.config;
    requires common.config;
    requires common.model;
    requires common.util;
    requires coredb;
    requires org.slf4j;
    requires springdata;
    requires spring.beans;
    requires spring.context;
    requires common.constants;
}
