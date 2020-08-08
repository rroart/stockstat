package roart.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<String> getCategories(MetaItem meta) {
        if (meta == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(meta.getPeriod()).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
    
}
