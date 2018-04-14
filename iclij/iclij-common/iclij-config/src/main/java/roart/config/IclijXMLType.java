package roart.config;

public class IclijXMLType {
    private Class type;
    
    private Object defaults;
    
    private String text;
    
    private String convert;

    public IclijXMLType(Class type, Object defaults, String text, String convert) {
        super();
        this.type = type;
        this.defaults = defaults;
        this.text = text;
        this.convert = convert;
    }
    
    public IclijXMLType(Class type, Object defaults, String text) {
        new IclijXMLType(type, defaults, text, null);
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

    public String getConvert() {
        return convert;
    }

    public void setConvert(String convert) {
        this.convert = convert;
    }
    
}
