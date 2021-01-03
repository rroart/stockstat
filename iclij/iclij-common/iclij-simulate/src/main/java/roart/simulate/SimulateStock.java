package roart.simulate;

import java.time.LocalDate;

import roart.common.util.MathUtil;

public class SimulateStock {
    public String id;

    public double price;

    public double count;

    public double buyprice;

    public double sellprice;

    public LocalDate buydate;

    public LocalDate selldate;

    public double weight;

    public String status;

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
        // TODO Auto-generated constructor stub
    }

    public String getId() {
        return id;
    }

    public String toString() {
        MathUtil mu = new MathUtil();
        return id + " " + mu.round(price, 3) + " " + count + " " + mu.round(buyprice,  3) + " " + mu.round(sellprice, 3) + " " + buydate + " " + selldate;  
    }
}

