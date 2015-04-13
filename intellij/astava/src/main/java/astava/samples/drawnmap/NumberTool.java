package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberTool extends AbstractTool {
    private static class Number extends JFormattedTextField implements Cell<BigDecimal>, CellConsumer<BigDecimal> {
        private Slot<BigDecimal> slot;

        public Number() {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setParseIntegerOnly(false);
            NumberFormatter formatter = new NumberFormatter(nf);
            formatter.setValueClass(BigDecimal.class);
            setFormatter(formatter);
            setValue(new BigDecimal(0));
            setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));

            slot = new Slot<>();
            slot.set(new BigDecimal(0));

            addPropertyChangeListener("value", evt -> {
                BigDecimal currentValue = (BigDecimal) getValue();
                if (currentValue != null)
                    slot.set(currentValue);
            });
        }

        @Override
        public void setBinding(Binding binding) {
            slot.setBinding(binding);
        }

        @Override
        public Binding consume(CellConsumer<BigDecimal> consumer) {
            return slot.consume(consumer);
        }

        @Override
        public void next(BigDecimal value) {
            slot.set(value);
            setValue(value);
        }

        @Override
        public void setDescription(Object description) {
            slot.setDescription(description);
        }

        @Override
        public Object getDescription() {
            return slot.getDescription();
        }
    }

    public NumberTool() {
        super("Number");
    }

    @Override
    public ToolSession startSession(int x1, int y1) {
        Number number = new Number();

        number.setLocation(x1, y1);

        getTarget().add(number);

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

                number.setSize(xDelta + 1, yDelta + 1);
                number.setLocation(left, top);
            }

            @Override
            public void end() {

            }
        };
    }
}
