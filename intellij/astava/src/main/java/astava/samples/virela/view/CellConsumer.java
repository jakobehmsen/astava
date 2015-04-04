package astava.samples.virela.view;

import java.util.function.Consumer;

public interface CellConsumer<T> {
    void next(T value, Consumer<CellConsumer> remain);
    void atEnd();
}
