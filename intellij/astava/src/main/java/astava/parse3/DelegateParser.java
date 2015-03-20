package astava.parse3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DelegateParser<T> implements Parser<T> {
    private Parser<T> parser;
    private ArrayList<Ref> refs;
    private Hashtable<String, Parser<T>> resolvedRefs;
    private boolean initializing;

    public DelegateParser() {
        initializing = true;
        refs = new ArrayList<>();
        parser = createParser();
        initializing = false;
    }

    protected abstract Parser<T> createParser();

    @Override
    public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
        return parser.parse(input, matcher);
    }

    private void ensureRefsProcessed() {
        if(resolvedRefs == null) {
            resolvedRefs = new Hashtable();

            IdentityHashMap<Parser<T>, String> refFieldToNameMap = new IdentityHashMap<>();
            try {
                for(Field f: getClass().getDeclaredFields()) {
                    if(Parser.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Parser<T> value = (Parser<T>) f.get(this);
                        if(value != null) {
                            refFieldToNameMap.put(value, f.getName());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            refs.forEach(r -> {
                Parser<T> p = r.parserSupplier.get();
                String name = refFieldToNameMap.get(p);
                if (name != null) {
                    resolvedRefs.put(name, p);
                    r.name = name;
                }
            });

            refs = null;
        }
    }

    @Override
    public String toString() {
        if(!initializing) {
            ensureRefsProcessed();

            String str = resolvedRefs.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).collect(Collectors.joining("\n"));

            return parser.toString() + " where \n" + str;
        } else
            return "Initializing...";
    }

    private class Ref implements Parser<T> {
        private Supplier<Parser<T>> parserSupplier;
        private String name;

        private Ref(Supplier<Parser<T>> parserSupplier) {
            this.parserSupplier = parserSupplier;
        }

        @Override
        public <R extends Matcher<T>> R parse(Input<T> input, R matcher) {
            Parser<T> parser = parserSupplier.get();
            R refMatcher = (R)matcher.beginVisit(parser, input);
            parser.parse(input, refMatcher);
            refMatcher.propagate(matcher);
            return matcher;
        }

        @Override
        public String toString() {
            return name != null ? name : "Ref";
        }
    }

    protected Parser<T> ref(Supplier<Parser<T>> parserSupplier) {
        Ref r = new Ref(parserSupplier);
        refs.add(r);
        return r;
    }
}
