package astava.parse2;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public interface ParseContext<Failure> {
    ParseContext<Failure> getParent();
    <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value);
    <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value);

    default <Failure> Iterator<ParseContext<Failure>> iterator() {
        return new Iterator<ParseContext<Failure>>() {
            ParseContext<Failure> next = (ParseContext<Failure>)ParseContext.this;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public ParseContext<Failure> next() {
                ParseContext<Failure> current = next;
                next = current.getParent();
                return current;
            }
        };
    }

    default Collection<ParseContext<Failure>> asCollection() {
        return new AbstractCollection<ParseContext<Failure>>() {
            @Override
            public Iterator<ParseContext<Failure>> iterator() {
                return ParseContext.this.iterator();
            }

            @Override
            public int size() {
                return (int)stream().count();
            }
        };
    }

    default Stream<ParseContext<Failure>> stream() {
        return asCollection().stream();
    }
}
