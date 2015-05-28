package astava.samples.misc;

import astava.parse.Atom;
import astava.parse.Node;
import astava.parse.Parse;
import astava.parse.charsequence.CharParse;

import java.util.stream.Collectors;

public class NumberParser extends astava.parse.DelegateParser<Character, Node> {
    @Override
    public astava.parse.Parser<Character, Node> createParser() {
        return CharParse.<Character>isDigit().then(Parse.copy()).then(Parse.consume()).onceOrMore().wrap((cursor, matcher) -> {
            return production -> {
                String strValue = production.stream().map(c -> "" + c).collect(Collectors.joining());
                int value = Integer.parseInt(strValue);
                matcher.put(new Atom(value));
            };
        });
    }
}
