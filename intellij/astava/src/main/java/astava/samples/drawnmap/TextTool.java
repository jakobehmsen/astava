package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;

public class TextTool extends AbstractTool {
    private static class Text extends JFormattedTextField implements Cell<String>, CellConsumer<String> {
        private Slot<String> slot;

        public Text() {
            setFormatter(new DefaultFormatter());
            setValue("");
            setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

            slot = new Slot<>();
            slot.set("");

            addPropertyChangeListener("value", evt -> {
                String currentValue = (String) getValue();
                if (currentValue != null)
                    slot.set(currentValue);
            });
        }

        @Override
        public void setBinding(Binding binding) {
            slot.setBinding(binding);
        }

        @Override
        public Binding consume(CellConsumer<String> consumer) {
            return slot.consume(consumer);
        }

        @Override
        public void next(String value) {
            slot.set(value);
            setValue(value);
        }
    }

    public TextTool() {
        super("Text");
    }

    @Override
    public ToolSession startSession(int x1, int y1) {
        Text text = new Text();

        text.setLocation(x1, y1);

        getTarget().add(text);

        return new ToolSession() {
            @Override
            public void update(int x2, int y2) {
                int left = Math.min(x1, x2);
                int right = Math.max(x1, x2);
                int top = Math.min(y1, y2);
                int bottom = Math.max(y1, y2);
                int xDelta = right - left;
                int yDelta = bottom - top;

                int xDir = x1 < x2
                    ? 0 // LeftRight
                    : 1 ; // RightLeft
                int yDir = y1 < y2
                    ? 0 // TopDown
                    : 1 ; // BottomUp

                text.setSize(xDelta + 1, yDelta + 1);
                text.setLocation(left, top);
            }

            @Override
            public void end() {

            }
        };
    }
}
