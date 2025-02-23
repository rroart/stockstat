/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb.spark {
    exports roart.db.spark;

    requires iclij.common.config;
    requires common.config;
    requires common.constants;
    requires common.model;
    requires common.util;
    requires coredb;
    requires scala.library;
    requires org.slf4j;
    requires stockstat.shadow.spark;
}
