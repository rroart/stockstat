/**
 * 
 */
/**
 * @author roart
 *
 */
module stocketl {
    exports roart.etl;
    exports roart.etl.db;

    requires dbdao;
    requires common.config;
    requires common.constants;
    requires common.model;
    requires common.util;
    requires stockutil;
    requires commons.lang;
    requires commons.math3;
    requires jcommon;
    requires jfreechart;
    requires model;
    requires slf4j.api;
    requires org.apache.commons.lang3;
    requires pipeline.model;
}
