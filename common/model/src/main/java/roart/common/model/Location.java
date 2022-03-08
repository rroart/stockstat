package roart.common.model;

import java.util.Objects;

public class Location {
    public String nodename;
    
    public String fs;
    
    public String extra;

    public Location() {
	super();
    }
    
    public Location(String nodename, String fs) {
        super();
        this.nodename = nodename;
        this.fs = fs;
    }

    public Location(String nodename, String fs, String extra) {
        super();
        this.nodename = nodename;
        this.fs = fs;
        this.extra = extra;
    }
    
    @Override
    public String toString() {
        return nullString(nodename) + ":" + nullString(fs) + ":" + nullString(extra);
    }
    
    private String nullString(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        if (o == this) {
            return true;
        }
  
        if (!(o instanceof Location)) {
            return false;
        }        
        
        Location ob = (Location) o;
        
        return Objects.equals(nodename, ob.nodename)
                && Objects.equals(fs, ob.fs)
                && Objects.equals(extra, ob.extra);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodename, fs, extra);
    }
}
