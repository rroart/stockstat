package roart.simulate.model;

import java.time.LocalDate;

import roart.common.util.MathUtil;

public class SimulateStock {
    private String id;

    private double price;

    private double count;

    private double buyprice;

    private double sellprice;

    private LocalDate buydate;

    private LocalDate selldate;

    private double weight;

    private String status;

    public SimulateStock(String id, double price, double count, double buyprice, double sellprice, LocalDate buydate,
            LocalDate selldate, double weight, String status) {
        super();
        this.id = id;
        this.price = price;
        this.count = count;
        this.buyprice = buyprice;
        this.sellprice = sellprice;
        this.buydate = buydate;
        this.selldate = selldate;
        this.weight = weight;
        this.status = status;
    }

    public SimulateStock copy() {
        SimulateStock copy = new SimulateStock();
        copy.id = id;
        copy.price = price;
        copy.count = count;
        copy.buyprice = buyprice;
        copy.sellprice = sellprice;
        copy.buydate = buydate;
        copy.selldate = selldate;
        copy.weight = weight;
        copy.status = status;
        return copy;
    }

    public SimulateStock() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getBuyprice() {
        return buyprice;
    }

    public void setBuyprice(double buyprice) {
        this.buyprice = buyprice;
    }

    public double getSellprice() {
        return sellprice;
    }

    public void setSellprice(double sellprice) {
        this.sellprice = sellprice;
    }

    public LocalDate getBuydate() {
        return buydate;
    }

    public void setBuydate(LocalDate buydate) {
        this.buydate = buydate;
    }

    public LocalDate getSelldate() {
        return selldate;
    }

    public void setSelldate(LocalDate selldate) {
        this.selldate = selldate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString() {
        MathUtil mu = new MathUtil();
        return id + " " + mu.round(price, 3) + " " + count + " " + mu.round(buyprice,  3) + " " + mu.round(sellprice, 3) + " " + buydate + " " + selldate;  
    }
}

