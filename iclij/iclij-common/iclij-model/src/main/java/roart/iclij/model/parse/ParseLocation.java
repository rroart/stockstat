package roart.iclij.model.parse;

public class ParseLocation {
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String l) {
        this.country = l;
    }

    public void show() {
        System.out.println("Location " + getCountry());
    }

}


