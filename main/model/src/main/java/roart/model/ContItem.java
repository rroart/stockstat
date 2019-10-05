package roart.model;

import roart.db.model.Cont;

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
    
    public void save() throws Exception {
        Cont cont = new Cont();
        cont.setDate(getDate());
        cont.setFilename(getFilename());
        cont.setMd5(getMd5());
        cont.save();
    }
    
    public static List<ContItem> getAll() throws Exception {
        List<Cont> conts = Cont.getAll();
        List<ContItem> contItems = new ArrayList<>();
        for (Cont cont : conts) {
            ContItem memoryItem = getContItem(cont);
            contItems.add(memoryItem);
        }
        return contItems;
    }

   public static List<ContItem> getAll(String market) throws Exception {
        List<Cont> conts = Cont.getAll(market);
        List<ContItem> configItems = new ArrayList<>();
        for (Cont cont : conts) {
            ContItem memoryItem = getContItem(cont);
            configItems.add(memoryItem);
        }
        return configItems;
    }

    private static ContItem getContItem(Cont config) {
        ContItem configItem = new ContItem();
        configItem.setDate(config.getDate());
        configItem.setFilename(config.getFilename());
        configItem.setMd5(config.getMd5());
        return configItem;
    }

}
