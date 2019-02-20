package roart.common.config;

public class XMLType {
    private Class type;
    
    private Object defaults;
    
    private String text;
    
    private Double[] range;
    
    public XMLType(Class type, Object defaults, String text) {
        super();
        this.type = type;
        this.defaults = defaults;
        this.text = text;
    }
    
    public XMLType(Class type, Object defaults, String text, Double[] doubles) {
        this(type, defaults, text);
        this.range = range;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public Object getDefaults() {
        return defaults;
    }

    public void setDefaults(Object defaults) {
        this.defaults = defaults;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double[] getRange() {
        return range;
    }

    public void setRange(Double[] range) {
        this.range = range;
    }
}
