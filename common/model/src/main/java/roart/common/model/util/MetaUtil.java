package roart.common.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import roart.common.constants.Constants;
import roart.common.model.MetaDTO;
import roart.common.pipeline.data.SerialMeta;

public class MetaUtil {

    public MetaDTO findMeta(List<MetaDTO> metas, String marketName) {
        for (MetaDTO meta : metas) {
            if (marketName.equals(meta.getMarketid())) {
                return meta;
            }
        }
        return null;
    }

    public List<String> getCategories(MetaDTO meta) {
        if (meta == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(meta.getPeriod()).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public Integer getCategory(MetaDTO meta, String name) {
        for (int i = 0; i < meta.getPeriod().length; i++) {
            if (name.equals(meta.getPeriod()[i])) {
                return i;
            }
        }
        return null;
    }
    
    public String getCategory(MetaDTO meta, Integer category) {
        String categoryName;
        switch (category) {
        case Constants.PRICECOLUMN:
            categoryName = Constants.PRICE;
            break;
        case Constants.INDEXVALUECOLUMN:
            categoryName = Constants.INDEX;
            break;
        default:
            return meta.getperiod(category);
        }
        return categoryName;
    }
    
    public String getCategory(SerialMeta meta, Integer category) {
        String categoryName;
        switch (category) {
        case Constants.PRICECOLUMN:
            categoryName = Constants.PRICE;
            break;
        case Constants.INDEXVALUECOLUMN:
            categoryName = Constants.INDEX;
            break;
        default:
            return meta.getPeriod()[category];
        }
        return categoryName;
    }
}
