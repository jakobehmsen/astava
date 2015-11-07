package astava.tree;

import java.util.Hashtable;

public class CodeDomComparison {
    private Hashtable<Object, Object> labels = new Hashtable<>();

    public Boolean isSameLabel(Object label1, Object label2) {
        labels.putIfAbsent(label1, label2);
        return labels.get(label1) == label2;
    }
}
