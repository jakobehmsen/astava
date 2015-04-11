package astava.samples.virela.view;

import java.util.Hashtable;
import java.util.Map;

public class Dict implements Cell<Object> {
    private Map<String, Slot<?>> slots = new Hashtable<>();

    public void put(String id, Cell<?> value) {
        Slot slot = slots.computeIfAbsent(id, i -> new Slot<>());

        slot.set(value);
    }

    public Cell<?> get(String id) {
        return slots.computeIfAbsent(id, i -> new Slot<>());
    }

    @Override
    public Binding consume(CellConsumer<Object> consumer) {
        return null;
    }
}
