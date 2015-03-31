package astava.parse;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Input<T> extends Iterable<T> {
    Cursor<T> cursor();

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
