package net.mehvahdjukaar.smarterfarmers;

import java.util.HashMap;

public class CountOrderedSortedMap<T> extends HashMap<T, Integer> {

    public void add(T value) {
        this.merge(value, -1, Integer::sum);
        //int count = -1;
        //if (this.containsKey(value)) count = this.get(value) - 1;
        //this.put(value, count);
    }
}
