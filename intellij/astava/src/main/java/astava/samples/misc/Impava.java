package astava.samples.misc;

import astava.parse.*;
import astava.parse.charsequence.CharParse;

import java.util.Map;

/**
 * IMPerative jAVA
 * A parser which parses, compiles, and evaluates each root statement one at a time.
 */
public class Impava extends DelegateParser<Character, Object> {
    private Map<String, Object> variables;

    public Impava(Map<String, Object> variables) {
        this.variables = variables;
    }

    private astava.parse.Parser<Character, Object> ws =
        new SkipParser<>(CharParse.isWhitespace().then(Parse.consume()).multi());

    private astava.parse.Parser<Character, Object> integer = Parse.reduceInt(
        CharParse.<Character>isDigit().then(Parse.copy()).then(Parse.consume()).onceOrMore(),
        i -> i
    );

    private astava.parse.Parser<Character, Object> string = Parse.reduceString(
        CharParse.<Character>isChar('\'').then(Parse.consume()).then(
            CharParse.<Character>isChar('\'').not().then(Parse.copy()).maybe().then(Parse.consume()).multi()
        ),
        str -> str
    );

    private astava.parse.Parser<Character, Object> expression = integer.or(string);

    private astava.parse.Parser<Character, Object> id = Parse.reduceString(
        CharParse.<Character>isLetter().then(Parse.copy()).then(Parse.consume()).onceOrMore(),
        str -> str
    );

    private astava.parse.Parser<Character, Object> assign =
        id.then(ws).then(CharParse.isChar('=')).then(Parse.consume()).then(ws).then(expression)
        .consume2((String id, Object value) -> {
            variables.put(id, value);
        });

    private astava.parse.Parser<Character, Object> statement = assign;
    private astava.parse.Parser<Character, Object> statements = statement.then(ws.then(statement).multi());
    private astava.parse.Parser<Character, Object> body = ws.then(statements).then(ws);

    @Override
    public Parser<Character, Object> createParser() {
        return ref(() -> this.body);
    }
}
