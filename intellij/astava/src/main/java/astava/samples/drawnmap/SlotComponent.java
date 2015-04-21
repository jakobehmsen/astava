package astava.samples.drawnmap;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.function.*;

public class SlotComponent extends JPanel implements Cell<Object>, CellConsumer<Object> {
    private Slot<Object> slot;
    private SlotValueComponent slotValue;
    private SlotValueComponentFactory slotValueFactory;

    public SlotComponent(SlotValueComponentFactory slotValueFactory) {
        slot = new Slot<>();
        setLayout(new BorderLayout());
        this.slotValueFactory = slotValueFactory;
    }

    @Override
    public void setBinding(Binding binding) {
        slot.setBinding(binding);
    }

    @Override
    public Binding consume(CellConsumer<Object> consumer) {
        return slot.consume(consumer);
    }

    @Override
    public Object value(Object[] args) {
        return slot.value(args);
    }

    @Override
    public void next(Object value) {
        if(slotValue == null)
            createSlotValueComponent(value);
        else if(!slotValue.accepts(value)) {
            remove(slotValue.getComponent());
            createSlotValueComponent(value);
        } else
            slotValue.setValue(value);
    }

    private void createSlotValueComponent(Object value) {
        slotValue = slotValueFactory.createSlotComponentValue(this, slot, value);

        setBounds(slotValue.getComponent().getBounds());
        add(slotValue.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void setDescription(Object description) {
        slot.setDescription(description);
    }

    @Override
    public Object getDescription() {
        return slot.getDescription();
    }

    @Override
    public Binding getBinding() {
        return slot.getBinding();
    }

    private Hashtable<String, Binding> propertyBindings = new Hashtable<>();

    private Consumer propertyUpdater(String name) {
        switch(name) {
            case "x":
                return value ->
                    setLocation(((BigDecimal) value).intValue(), getY());
            case "y":
                return value ->
                    setLocation(getX(), ((BigDecimal) value).intValue());
            case "width":
                return value -> {
                    setSize(((BigDecimal) value).intValue(), getHeight());
                    revalidate();
                    repaint();
                };
            case "height":
                return value -> {
                    setSize(getWidth(), ((BigDecimal) value).intValue());
                    revalidate();
                    repaint();
                };
        }

        return null;
    }

    public void propertyAssign(String name, Cell<Object> valueCell) {
        Binding binding = propertyBindings.get(name);

        if(binding != null)
            binding.remove();

        Consumer propertyUpdater = propertyUpdater(name);
        binding = valueCell.consume(value -> propertyUpdater.accept(value));

        propertyBindings.put(name, binding);
    }

    private abstract class PropertyCell implements Cell {
        ArrayList<CellConsumer> consumers = new ArrayList<>();

        @Override
        public Binding consume(CellConsumer consumer) {
            consumers.add(consumer);
            consumer.next(value(null));
            return () -> {
                consumers.remove(consumer);
                if(consumers.isEmpty())
                    clean();
            };
        }

        protected void post() {
            consumers.forEach(x -> x.next(value(null)));
        }

        protected abstract void clean();
    }

    private abstract class ComponentListerPropertyCell extends PropertyCell {
        Object lastValue;

        ComponentListener listener = new ComponentAdapter() {
            {
                lastValue = value(null);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                componentChanged();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                componentChanged();
            }
        };

        {
            addComponentListener(listener);
        }

        @Override
        protected void clean() {
            removeComponentListener(listener);
        }

        protected void componentChanged() {
            if(lastValue == null || !lastValue.equals(value(null)));
                post();
            lastValue = value(null);
        }
    }

    public Cell property(String name) {
        switch(name) {
            case "x":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value(Object[] args) {
                        return new BigDecimal(getX());
                    }
                };
            case "y":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value(Object[] args) {
                        return new BigDecimal(getY());
                    }
                };
            case "width":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value(Object[] args) {
                        return new BigDecimal(getWidth());
                    }
                };
            case "height":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value(Object[] args) {
                        return new BigDecimal(getHeight());
                    }
                };
        }

        return null;
    }
}
