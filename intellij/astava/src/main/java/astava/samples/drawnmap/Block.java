package astava.samples.drawnmap;

public class Block {
    public final Cell cell;

    public Block(Cell cell) {
        this.cell = cell;
    }

    @Override
    public String toString() {
        return "{...}";
    }
}
