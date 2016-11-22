package roart.db;

import roart.model.ResultItem;

import java.util.List;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract String classify(String type, String language);

}

