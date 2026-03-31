package roart.model.data;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import roart.common.model.StockDTO;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
        */
public class StockData {

    public String[] periodText;
    public Map<String, MarketData> marketdatamap;
    public List<StockDTO>[] datedstocklists;
    public List<String> stockdates;
    public Map<String, List<StockDTO>> stockdatemap;
    public Map<String, String> idNameMap;
    public Integer cat;
    public String catName;
    public List<StockDTO> datedstocks;
    public Integer days;
    public Map<String, List<StockDTO>> stockidmap;

    public StockData() {
        super();
    }

    // Github Copilot

    /**
     * Compares this StockData object with another StockData object.
     * Returns true if all fields are equal, false otherwise.
     * 
     * @param other the StockData object to compare with
     * @return true if all fields are equal, false otherwise
     */
    public boolean compare(StockData other) {
        if (other == null) {
            return false;
        }
        
        // Compare primitive/wrapper fields
        if (!Objects.equals(this.cat, other.cat)) {
            return false;
        }
        if (!Objects.equals(this.catName, other.catName)) {
            return false;
        }
        if (!Objects.equals(this.days, other.days)) {
            return false;
        }
        
        // Compare arrays
        if (!Arrays.equals(this.periodText, other.periodText)) {
            return false;
        }
        
        // Compare List<String>
        if (!Objects.equals(this.stockdates, other.stockdates)) {
            return false;
        }
        
        // Compare List<StockDTO>
        if (!Objects.equals(this.datedstocks, other.datedstocks)) {
            return false;
        }
        
        // Compare Map<String, String>
        if (!Objects.equals(this.idNameMap, other.idNameMap)) {
            return false;
        }
        
        // Compare Map<String, MarketData>
        if (!Objects.equals(this.marketdatamap, other.marketdatamap)) {
            return false;
        }
        
        // Compare Map<String, List<StockDTO>>
        if (!Objects.equals(this.stockdatemap, other.stockdatemap)) {
            return false;
        }
        
        // Compare Map<String, List<StockDTO>>
        if (!Objects.equals(this.stockidmap, other.stockidmap)) {
            return false;
        }
        
        // Compare List<StockDTO>[] array
        if (!Arrays.deepEquals(this.datedstocklists, other.datedstocklists)) {
            return false;
        }
        
        return true;
    }

    /**
     * Compares the sizes of maps and lists between this StockData object and another.
     * Returns a string representation of the comparison results.
     * 
     * @param other the StockData object to compare with
     * @return a string describing the size comparison results
     */
    public String compareMapAndListSizes(StockData other) {
        if (other == null) {
            return "Other StockData object is null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Size Comparison between StockData objects:\n");
        
        // Compare periodText array size
        int thisPerioTextSize = this.periodText != null ? this.periodText.length : 0;
        int otherPerioTextSize = other.periodText != null ? other.periodText.length : 0;
        sb.append("periodText size: ").append(thisPerioTextSize).append(" vs ").append(otherPerioTextSize);
        sb.append(thisPerioTextSize == otherPerioTextSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare marketdatamap size
        int thisMarketdatamapSize = this.marketdatamap != null ? this.marketdatamap.size() : 0;
        int otherMarketdatamapSize = other.marketdatamap != null ? other.marketdatamap.size() : 0;
        sb.append("marketdatamap size: ").append(thisMarketdatamapSize).append(" vs ").append(otherMarketdatamapSize);
        sb.append(thisMarketdatamapSize == otherMarketdatamapSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare datedstocklists array size
        int thisDatedstocklistsSize = this.datedstocklists != null ? this.datedstocklists.length : 0;
        int otherDatedstocklistsSize = other.datedstocklists != null ? other.datedstocklists.length : 0;
        sb.append("datedstocklists size: ").append(thisDatedstocklistsSize).append(" vs ").append(otherDatedstocklistsSize);
        sb.append(thisDatedstocklistsSize == otherDatedstocklistsSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare stockdates list size
        int thisStockdatesSize = this.stockdates != null ? this.stockdates.size() : 0;
        int otherStockdatesSize = other.stockdates != null ? other.stockdates.size() : 0;
        sb.append("stockdates size: ").append(thisStockdatesSize).append(" vs ").append(otherStockdatesSize);
        sb.append(thisStockdatesSize == otherStockdatesSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare stockdatemap size
        int thisStockdatemapSize = this.stockdatemap != null ? this.stockdatemap.size() : 0;
        int otherStockdatemapSize = other.stockdatemap != null ? other.stockdatemap.size() : 0;
        sb.append("stockdatemap size: ").append(thisStockdatemapSize).append(" vs ").append(otherStockdatemapSize);
        sb.append(thisStockdatemapSize == otherStockdatemapSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare idNameMap size
        int thisIdNameMapSize = this.idNameMap != null ? this.idNameMap.size() : 0;
        int otherIdNameMapSize = other.idNameMap != null ? other.idNameMap.size() : 0;
        sb.append("idNameMap size: ").append(thisIdNameMapSize).append(" vs ").append(otherIdNameMapSize);
        sb.append(thisIdNameMapSize == otherIdNameMapSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare datedstocks list size
        int thisDatedstocksSize = this.datedstocks != null ? this.datedstocks.size() : 0;
        int otherDatedstocksSize = other.datedstocks != null ? other.datedstocks.size() : 0;
        sb.append("datedstocks size: ").append(thisDatedstocksSize).append(" vs ").append(otherDatedstocksSize);
        sb.append(thisDatedstocksSize == otherDatedstocksSize ? " [MATCH]\n" : " [MISMATCH]\n");
        
        // Compare stockidmap size
        int thisStockidmapSize = this.stockidmap != null ? this.stockidmap.size() : 0;
        int otherStockidmapSize = other.stockidmap != null ? other.stockidmap.size() : 0;
        sb.append("stockidmap size: ").append(thisStockidmapSize).append(" vs ").append(otherStockidmapSize);
        sb.append(thisStockidmapSize == otherStockidmapSize ? " [MATCH]" : " [MISMATCH]");
        
        return sb.toString();
    }

    /**
     * Compares the sizes of maps and lists between this StockData object and another.
     * Returns true if all sizes match, false if any size differs or other is null.
     * 
     * @param other the StockData object to compare with
     * @return true if all map and list sizes match, false otherwise
     */
    public boolean compareMapAndListSizesBoolean(StockData other) {
        if (other == null) {
            return false;
        }
        
        // Compare periodText array size
        int thisPerioTextSize = this.periodText != null ? this.periodText.length : 0;
        int otherPerioTextSize = other.periodText != null ? other.periodText.length : 0;
        if (thisPerioTextSize != otherPerioTextSize) {
            return false;
        }
        
        // Compare marketdatamap size
        int thisMarketdatamapSize = this.marketdatamap != null ? this.marketdatamap.size() : 0;
        int otherMarketdatamapSize = other.marketdatamap != null ? other.marketdatamap.size() : 0;
        if (thisMarketdatamapSize != otherMarketdatamapSize) {
            return false;
        }
        
        // Compare datedstocklists array size
        int thisDatedstocklistsSize = this.datedstocklists != null ? this.datedstocklists.length : 0;
        int otherDatedstocklistsSize = other.datedstocklists != null ? other.datedstocklists.length : 0;
        if (thisDatedstocklistsSize != otherDatedstocklistsSize) {
            return false;
        }
        
        // Compare stockdates list size
        int thisStockdatesSize = this.stockdates != null ? this.stockdates.size() : 0;
        int otherStockdatesSize = other.stockdates != null ? other.stockdates.size() : 0;
        if (thisStockdatesSize != otherStockdatesSize) {
            return false;
        }
        
        // Compare stockdatemap size
        int thisStockdatemapSize = this.stockdatemap != null ? this.stockdatemap.size() : 0;
        int otherStockdatemapSize = other.stockdatemap != null ? other.stockdatemap.size() : 0;
        if (thisStockdatemapSize != otherStockdatemapSize) {
            return false;
        }
        
        // Compare idNameMap size
        int thisIdNameMapSize = this.idNameMap != null ? this.idNameMap.size() : 0;
        int otherIdNameMapSize = other.idNameMap != null ? other.idNameMap.size() : 0;
        if (thisIdNameMapSize != otherIdNameMapSize) {
            return false;
        }
        
        // Compare datedstocks list size
        int thisDatedstocksSize = this.datedstocks != null ? this.datedstocks.size() : 0;
        int otherDatedstocksSize = other.datedstocks != null ? other.datedstocks.size() : 0;
        if (thisDatedstocksSize != otherDatedstocksSize) {
            return false;
        }
        
        // Compare stockidmap size
        int thisStockidmapSize = this.stockidmap != null ? this.stockidmap.size() : 0;
        int otherStockidmapSize = other.stockidmap != null ? other.stockidmap.size() : 0;
        if (thisStockidmapSize != otherStockidmapSize) {
            return false;
        }
        
        return true;
    }
}
