/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb.spring {
    exports roart.db.spring;

    requires common.config;
    requires common.model;
    requires common.util;
    requires pipeline.model;
    requires model;
    requires coredb;
    requires org.slf4j;
    requires springdata;
    requires spring.beans;
    requires spring.context;
    requires common.constants;
}
