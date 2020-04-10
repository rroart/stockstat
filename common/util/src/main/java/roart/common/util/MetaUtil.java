package roart.common.util;

import java.util.List;

import roart.common.model.MetaItem;

public class MetaUtil {

    public MetaItem findMeta(List<MetaItem> metas, String marketName) {
        for (MetaItem meta : metas) {
            if (marketName.equals(meta.getMarketid())) {
                return meta;
            }
        }
        return null;
    }

}
