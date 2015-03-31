package astava.parse;

import java.util.List;

public class ListInput<T> implements Input<T> {
    private List<T> list;

    public ListInput(List<T> list) {
        this.list = list;
    }

    @Override
    public Cursor<T> cursor() {
        return new ListCursor<>(list);
    }
}
