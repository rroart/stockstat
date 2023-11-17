package roart.common.util;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public     class Arr extends MyObject {
    public Arr() {
        super();
    }
    
    public  double[][] arra;
}

