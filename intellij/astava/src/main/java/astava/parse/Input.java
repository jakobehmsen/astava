package astava.parse;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Input<T> extends Iterable<T> {
    Cursor<T> cursor();

    default void take1(Consumer<T> consumer) {
        consumer.accept(cursor().peek());
    }

    default void take2(BiConsumer<T, T> consumer) {
        Cursor<T> cursor = cursor();
        T first = cursor.peek();
        cursor.consume();
        T second = cursor.peek();
        consumer.accept(first, second);
    }

    default Iterator<T> iterator() {
        Cursor<T> cursor = cursor();

        return new Iterator<T>() {
            T next;

            {
                moveNext();
            }

            private void moveNext() {
                if(!cursor.atEnd()) {
                    next = cursor.peek();
                    cursor.consume();
                } else
                    next = null;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                T value = next;
                moveNext();
                return value;
            }
        };
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
