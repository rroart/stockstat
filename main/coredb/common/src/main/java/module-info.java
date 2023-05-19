/**
 * 
 */
/**
 * @author roart
 *
 */
module coredb {
    exports roart.db.common;

    requires iclij.common.config;
    requires common.config;
    requires common.model;
    requires model;
    requires org.slf4j;
    requires pipeline.model;
}
