package parse;

import astava.core.Atom;

import java.util.Map;

public class AtomParser implements Parser {
    @Override
    public void parse(Matcher matcher, Map<String, Parser> rules) {
        if(Character.isLetter(matcher.peekByte())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append((char)matcher.peekByte());
            matcher.consume();

            while(Character.isLetter(matcher.peekByte())) {
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();
            }

            matcher.put(new Atom(stringBuilder.toString()));
            matcher.match();
        }
    }
}
