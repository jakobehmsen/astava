package astava.tree;

import java.util.Collections;
import java.util.List;

public interface Dom {
    default List<? extends Dom> getChildren() {
        return Collections.emptyList();
    }
    default Dom setChildren(List<? extends Dom> children) {
        return this;
    }
}
