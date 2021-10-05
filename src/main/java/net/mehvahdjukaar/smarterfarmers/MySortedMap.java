package net.mehvahdjukaar.smarterfarmers;

import java.util.HashMap;
import java.util.TreeMap;

public class MySortedMap <T> extends HashMap<T, Integer> {

    public void add(T value) {
        Integer count = -1;
        if(this.containsKey(value)) count --;
        this.put(value, count);
    }
}
