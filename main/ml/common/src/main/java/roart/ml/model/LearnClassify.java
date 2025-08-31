package roart.ml.model;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.DoubleArraySerializer;

public class LearnClassify {
    private String id;
    
    private Object array;
    
    private Double classification;

    // for jackson
    public LearnClassify() {
        super();
    }

    public LearnClassify(String id, Object array, Double classification) {
        super();
        this.id = id;
        this.array = array;
        this.classification = classification;
    }

    public LearnClassify(String id, Object array, Integer classification) {
        super();
        this.id = id;
        this.array = array;
        this.classification = classification != null ? classification.doubleValue() : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getArray() {
        return array;
    }

    public void setArray(double[] array) {
        this.array = array;
    }

    public Double getClassification() {
        return classification;
    }

    public void setClassification(Double classification) {
        this.classification = classification;
    }
    
    @Override
    public String toString() {
        return "LearnClassify [id=" + id + ", classification=" + classification + ", dim=(" + getArrayDimString(array) + "), array=" + getArrayString(array) + "]\n";
    }

    // from github copilot
    private String getArrayString(Object array) {
        if (array == null) {
            return "null";
        }
        if (array instanceof double[]) {
            double[] arr = (double[]) array;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i < arr.length - 1) sb.append(", ");
            }
            sb.append("]\n");
            return sb.toString();
        } else if (array instanceof double[][]) {
            double[][] arr = (double[][]) array;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(getArrayString(arr[i]));
                if (i < arr.length - 1) sb.append(", ");
            }
            sb.append("]\n");
            return sb.toString();
        } else if (array instanceof double[][][]) {
            double[][][] arr = (double[][][]) array;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(getArrayString(arr[i]));
                if (i < arr.length - 1) sb.append(", ");
            }
            sb.append("]\n");
            return sb.toString();
        } else {
            return array.toString();
        }
    }
    
    private String getArrayDimString(Object array) {
        if (array == null) {
            return "null";
        }
        if (array instanceof double[]) {
            double[] arr = (double[]) array;
            StringBuilder sb = new StringBuilder("");
            sb.append("" + arr.length);
            return sb.toString();
        } else if (array instanceof double[][]) {
            double[][] arr = (double[][]) array;
            StringBuilder sb = new StringBuilder("");
            sb.append("" + arr.length);
            sb.append(", ");
            sb.append(getArrayDimString(arr[0]));
            return sb.toString();
        } else if (array instanceof double[][][]) {
            double[][][] arr = (double[][][]) array;
            StringBuilder sb = new StringBuilder("");
            sb.append("" + arr.length);
            sb.append(", ");
            sb.append(getArrayDimString(arr[0]));
            return sb.toString();
        } else {
            return array.toString();
        }
    }

    // Returns the sizes of each dimension of the array as a string (e.g., "3x4x5")
    public String getArrayDim(Object array) {
        if (array == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        Object current = array;
        while (current != null && current.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(current);
            sb.append(len);
            // Move to the next dimension if possible
            if (len > 0) {
                current = java.lang.reflect.Array.get(current, 0);
                if (current != null && current.getClass().isArray()) {
                    sb.append(", ");
                }
            } else {
                // If the array is empty, we can't determine further dimensions
                break;
            }
        }
        return sb.toString();
    }
}