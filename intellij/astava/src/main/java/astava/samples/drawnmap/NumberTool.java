package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberTool implements Tool {
    @Override
    public String getText() {
        return "Number";
    }

    @Override
    public ToolSession startSession(JComponent target, int x1, int y1) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setParseIntegerOnly(false);
        NumberFormatter formatter = new NumberFormatter(nf);
        formatter.setValueClass(BigDecimal.class);
        JFormattedTextField number = new JFormattedTextField(formatter);
        number.setValue(new BigDecimal(0));
        number.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        number.setLocation(x1, y1);

        target.add(number);

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
