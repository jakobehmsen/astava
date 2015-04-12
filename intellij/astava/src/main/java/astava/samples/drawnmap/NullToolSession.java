package astava.samples.drawnmap;

public class NullToolSession implements ToolSession {
    public static final NullToolSession INSTANCE = new NullToolSession();

    private NullToolSession() { }

    @Override
    public void update(int x, int y) {

    }

    @Override
    public void end() {

    }
}
