package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque {

    Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    public T max() {
        T maxItem = (T) get(0);
        for (int i = 0; i < size(); ++i) {
            if (comparator.compare(maxItem, (T) get(i)) > 0) {
                maxItem = (T) get(i);
            }
        }
        return maxItem;
    }

    public T max(Comparator<T> c) {
        T maxItem = (T) get(0);
        for (int i = 0; i < size(); ++i) {
            if (c.compare(maxItem, (T) get(i)) > 0) {
                maxItem = (T) get(i);
            }
        }
        return maxItem;
    }
}
