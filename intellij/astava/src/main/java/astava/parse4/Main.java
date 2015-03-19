package astava.parse4;

public class Main {
    public static void main(String[] args) {
        Parser<Character> p = null;
        
        Matcher<Character> m = p.parse(null);


    }

    private static void process(Matcher<Character> m) {
        while(m.hasNext()) {
            m.moveNext();

        }
    }
}
