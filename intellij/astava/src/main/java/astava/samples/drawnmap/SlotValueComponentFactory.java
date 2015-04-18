package astava.samples.drawnmap;

import javax.swing.*;

public interface SlotValueComponentFactory {
    SlotValueComponent createSlotComponentValue(JPanel wrapper, Slot slot, Object value);
}
