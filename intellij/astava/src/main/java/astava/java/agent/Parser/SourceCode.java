package astava.java.agent.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SourceCode {
    public String text;
    public List<Object> captures;

    public SourceCode(String text, Object... captures) {
        this.text = text;
        this.captures = new ArrayList<>(Arrays.asList(captures));
    }
}
