package roart.iclij.model.parse;

public class ParseData extends ParseObject {
    private ParseTime time;

    private ParseLocation location;

    private ParseUnit unit;

    public ParseLocation getLocation() {
        return location;
    }

    public void setLoc(ParseLocation location) {
        this.location = location;
    }

    public ParseTime getTime() {
        return time;
    }

    public void setTime(ParseTime time) {
        this.time = time;
    }

    public ParseUnit getUnit() {
        return unit;
    }

    public void setUnit(ParseUnit unit) {
        this.unit = unit;
    }

    public void show() {
        log.debug("MyData show");
        if (getTime() != null) {
            getTime().show();
        }
        if (getLocation() != null) {
            getLocation().show();
        }
        if (getUnit() != null) {
            getUnit().show();
        }
    }

}

