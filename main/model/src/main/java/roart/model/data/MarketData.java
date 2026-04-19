package roart.model.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import roart.common.model.MetaDTO;
import roart.common.model.StockDTO;

public class MarketData {
    public List<StockDTO> stocks;
    public String[] periodtext;
    public List<StockDTO>[] datedstocklists;
    public MetaDTO meta;
    public List<String> ids;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof MarketData)) return false;
        MarketData that = (MarketData) o;

        if (!Objects.equals(this.stocks, that.stocks)) {
            System.out.println("mstock " + this.stocks + " " + that.stocks);
            return false;
        }
        if (!Objects.equals(this.datedstocklists.length, that.datedstocklists.length)) {
            System.out.println("mdat " + this.datedstocklists.length + " " + that.datedstocklists.length);
            return false;
        }
        for (int i = 0; i < this.datedstocklists.length; i++) {
            if (!Objects.equals(this.datedstocklists[i].size(), that.datedstocklists[i].size())) {
                System.out.println("mlen " + i + " " + this.datedstocklists[i].size() + " " + that.datedstocklists[i].size());
                //return false;
            }
        }
        for (int i = 0; i < this.datedstocklists.length; i++) {
            if (!Objects.equals(this.datedstocklists[i], that.datedstocklists[i])) {
                System.out.println("mlist " + this.datedstocklists[i] + " " + that.datedstocklists[i]);
                //return false;
            }
        }
            /*
            if (!Objects.equals(m1.datedstocklists, m2.datedstocklists)) {
                System.out.println(m1.datedstocklists + " " + m2.datedstocklists);
                return false;
            }

             */
        if (!Objects.equals(this.meta, that.meta)) {
            System.out.println(this.meta + " " + that.meta);
            return false;
        }
        if (!Arrays.equals(this.periodtext, that.periodtext)) {
            System.out.println(this.periodtext + " " + that.periodtext);
            return false;
        }
        if (!Objects.equals(this.ids, that.ids)) {
            System.out.println(this.ids + " " + that.ids);
            return false;
        }
        return true;
    }
}
