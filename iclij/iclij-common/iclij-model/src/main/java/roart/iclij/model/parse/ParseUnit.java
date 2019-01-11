package roart.iclij.model.parse;

public class ParseUnit extends ParseObject {

    public enum UNIT { STOCK, FUND, COMMODITY }

    private UNIT unit;

    private Number number; 

    private boolean plural;

    private boolean high;

    private boolean low;

    public UNIT getUnit() {
        return unit;
    }

    public void setUnit(UNIT unit) {
        this.unit = unit;
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public boolean isPlural() {
        return plural;
    }

    public void setPlural(boolean plural) {
        this.plural = plural;
    }

    public boolean isHigh() {
        return high;
    }

    public void setHigh(boolean high) {
        this.high = high;
    }

    public boolean isLow() {
        return low;
    }

    public void setLow(boolean low) {
        this.low = low;
    }

    public static final String STOCK = "stock";

    public static final String FUND = "fund";

    public static final String COMMODITY = "commodity";

    public static final String COMMODITIE = "commoditie";

    public static UNIT getUnit(String word) {
        UNIT unit = null;
        switch (word) {
        case COMMODITY:
        case COMMODITIE:
            unit = UNIT.COMMODITY;
            break;
        case FUND:
            unit = UNIT.FUND;
            break;
        case STOCK:
            unit = UNIT.STOCK;
            break;
        }
        return unit;
    }

    public void show() {
        System.out.print("Unit " + getUnit());
        System.out.print(" Plural" + isPlural());
        System.out.print(" Number " + getNumber());
        System.out.print(" Low " + isLow());
        System.out.println(" High " + isHigh());
    }

}


