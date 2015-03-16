package astava.parse2;

import java.util.ArrayList;
import java.util.List;

public class Multi<T, R, S> implements Parser<T, List<R>, S> {
    private Parser<T, R, S> parser;

    public Multi(Parser<T, R, S> parser) {
        this.parser = parser;
    }

    @Override
    public ParseResult<T, List<R>, S> parse(ParseContext<S> ctx, Source<T> source) {
        ArrayList<R> values = new ArrayList<>();

        while(true) {
            ParseResult<T, R, S> result = parser.parse(ctx, source);

            if(result.isSuccess()) {
                values.add(result.getValueIfSuccess());
                source = result.getSource();
            } else {
                // How to forward this failure?
                // A multi must end in failure
                // This failure may be important later

                return ctx
                    .success(source, values)
                    .trackFailure(source, result.getValueIfFailure());
            }
        }

        //return new ParseSuccess<T, List<R>, S>(source, values);
    }
}
