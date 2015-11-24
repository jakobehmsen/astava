package astava.java.agent.Parser;

import java.util.*;

public class SourceCode {
    public String text;
    public Map<String, Object> captures;

    public SourceCode(String text, Map<String, Object> captures) {
        this.text = text;
        this.captures = captures;
    }
}
