package net.mehvahdjukaar.smarterfarmers;

import java.util.HashMap;

public class CountOrderedSortedMap<T> extends HashMap<T, Integer> {

    public void add(T value) {
        int count = -1;
        if (this.containsKey(value)) count = this.get(value) - 1;
        this.put(value, count);
    }
}
