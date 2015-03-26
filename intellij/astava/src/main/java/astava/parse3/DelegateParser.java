package astava.parse3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DelegateParser<TIn, TOut> implements Parser<TIn, TOut> {
    private Parser<TIn, TOut> parser;
    private ArrayList<Ref> refs;
    private Hashtable<String, Parser<TIn, TOut>> resolvedRefs;

    public DelegateParser() {
        refs = new ArrayList<>();
        parser = createParser();
    }

    protected abstract Parser<TIn, TOut> createParser();

    @Override
    public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
        parser.parse(cursor, matcher);
    }

    private Hashtable<String, Parser<TIn, TOut>> resolvedRefs() {
        Hashtable<String, Parser<TIn, TOut>> resolvedRefs = new Hashtable();

        IdentityHashMap<Parser<TIn, TOut>, String> refFieldToNameMap = new IdentityHashMap<>();
        try {
            for(Field f: getClass().getDeclaredFields()) {
                if(Parser.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Parser<TIn, TOut> value = (Parser<TIn, TOut>) f.get(this);
                    if(value != null) {
                        refFieldToNameMap.put(value, f.getName());
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        refs.forEach(r -> {
            Parser<TIn, TOut> p = r.parserSupplier.get();
            String name = refFieldToNameMap.get(p);
            if (name != null) {
                resolvedRefs.put(name, p);
                r.name = name;
            }
        });

        return resolvedRefs;
    }

    @Override
    public String toString() {
        String str = resolvedRefs().entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).collect(Collectors.joining("\n"));

        return parser.toString() + " where \n" + str;
    }

    private class Ref implements Parser<TIn, TOut> {
        private Supplier<Parser<TIn, TOut>> parserSupplier;
        private String name;

        private Ref(Supplier<Parser<TIn, TOut>> parserSupplier) {
            this.parserSupplier = parserSupplier;
        }

        @Override
        public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
            Parser<TIn, TOut> parser = parserSupplier.get();
            Matcher<TIn, TOut> refMatcher = matcher.beginVisit(parser, cursor);
            parser.parse(cursor, refMatcher);
            refMatcher.propagate(matcher);
        }

        @Override
        public String toString() {
            return name != null ? name : "Ref";
        }
    }

    protected Parser<TIn, TOut> ref(Supplier<Parser<TIn, TOut>> parserSupplier) {
        Ref r = new Ref(parserSupplier);
        refs.add(r);
        return r;
    }
}
