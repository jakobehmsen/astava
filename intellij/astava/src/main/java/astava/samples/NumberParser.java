package astava.samples;

import astava.core.Atom;
import astava.core.Node;
import astava.parse3.Parse;
import astava.parse3.charsequence.CharParse;

import java.util.stream.Collectors;

public class NumberParser extends astava.parse3.DelegateParser<Character, Node> {
    @Override
    public astava.parse3.Parser<Character, Node> createParser() {
        return CharParse.<Character>isDigit().then(Parse.copy()).then(Parse.consume()).onceOrMore().wrap((cursor, matcher) -> {
            return production -> {
                String strValue = production.stream().map(c -> "" + c).collect(Collectors.joining());
                int value = Integer.parseInt(strValue);
                matcher.put(new Atom(value));
            };
        });
    }
}