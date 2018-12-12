package roart.config;

public class XMLType {
    private Class type;
    
    private Object defaults;
    
    private String text;
    
    public XMLType(Class type, Object defaults, String text) {
        super();
        this.type = type;
        this.defaults = defaults;
        this.text = text;
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
}
