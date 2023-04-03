package roart.common.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContItem {
    private String md5;
    
    private String filename;
    
    private LocalDate date;
    
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "" + date + " " + filename + " " + md5 + "\n"; 
    }

}
