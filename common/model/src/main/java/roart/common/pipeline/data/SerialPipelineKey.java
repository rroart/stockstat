package roart.common.pipeline.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class SerialPipelineKey {
    private static final int len = 5;

    private String[] key = new String[5];

    public SerialPipelineKey() {
        // for jackson
    }

    public SerialPipelineKey(String[] key) {
        this.key = key;
    }

    // for jackson
    public String[] getKey() {
        return key;
    }

    // for jackson
    public void setKey(String[] key) {
        this.key = key;
    }

    @JsonIgnore
    public String getFirst() {
        return key[0];
    }

    @JsonIgnore
    private String[] getRest() {
        return Arrays.copyOfRange(key, 1, key.length);
    }

    @JsonIgnore
    private String[] getAlmostLast() {
        return Arrays.copyOfRange(key, 0, key.length - 1);
    }

    public SerialPipelineKey rotateLeft(String element) {
        return new SerialPipelineKey(ArrayUtils.add(getRest(), element));
    }

    public SerialPipelineKey rotateRight(String element) {
        return new SerialPipelineKey(ArrayUtils.addFirst(getAlmostLast(), element));
    }

    public SerialPipelineKey rotateRight(String element, String secondElement) {
        SerialPipelineKey newKey = new SerialPipelineKey(ArrayUtils.addFirst(getAlmostLast(), secondElement));
        return new SerialPipelineKey(ArrayUtils.addFirst(newKey.getAlmostLast(), element));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SerialPipelineKey)) {
            return false;
        }
        //System.out.println("cmp"+((SerialPipelineKey) o).toString() + " " + this.toString());
        return Arrays.equals(((SerialPipelineKey) o).key, key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return "PipelineKey " + ArrayUtils.toString(key);
    }

    public boolean matches(SerialPipelineKey otherKey) {
        boolean found = true;
        for (int i = 0; i < key.length; i++) {
            if (key[i] != null && !Objects.equals(key[i], otherKey.key[i])) {
                found = false;
            }
        }
        return found;
    }

    @Override
    public SerialPipelineKey clone() {
        return new SerialPipelineKey(key.clone());
    }
}
