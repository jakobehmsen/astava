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
    //private BiFunction<Slot, Object, SlotValueComponent> slotValueFactory;
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
    public Object value() {
        return slot.value();
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
        //slotValue = slotValueFactory.apply(slot, value);
        slotValue = slotValueFactory.createSlotComponentValue(this, slot, value);

        /*if(value instanceof BigDecimal)
            slotValue = createSlotNumber((BigDecimal) value);
        else if(value instanceof String)
            slotValue = createSlotText((String) value);
        else if(value instanceof Line)
            slotValue = createSlotLine((Line) value);*/

        setBounds(slotValue.getComponent().getBounds());
        add(slotValue.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private SlotValueComponent createSlotNumber(BigDecimal value) {
        return new SlotValueComponent() {
            private JFormattedTextField component;

            {
                NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                nf.setParseIntegerOnly(false);
                NumberFormatter formatter = new NumberFormatter(nf);
                formatter.setValueClass(BigDecimal.class);
                component = new JFormattedTextField(formatter);

                component.addPropertyChangeListener("value", evt -> {
                    BigDecimal currentValue = (BigDecimal) component.getValue();
                    if (currentValue != null)
                        slot.set(currentValue);
                });

                component.setValue(value);
                component.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof BigDecimal;
            }

            @Override
            public void setValue(Object value) {
                component.setValue(value);
            }
        };
    }

    private SlotValueComponent createSlotText(String value) {
        return new SlotValueComponent() {
            private JFormattedTextField component;

            {
                component = new JFormattedTextField(new DefaultFormatter());

                component.addPropertyChangeListener("value", evt -> {
                    String currentValue = (String) component.getValue();
                    if (currentValue != null)
                        slot.set(currentValue);
                });

                component.setValue(value);
                component.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof String;
            }

            @Override
            public void setValue(Object value) {
                component.setValue(value);
            }
        };
    }

    private SlotValueComponent createSlotLine(Line value) {
        return new SlotValueComponent() {
            private LineTool.Line component;

            {
                component = new LineTool.Line(value.x1, value.y1, value.x2, value.y2);
                setBounds(component.getBounds());
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean accepts(Object value) {
                return value instanceof Line;
            }

            @Override
            public void setValue(Object value) {
                component.setLine(((Line) value).x1, ((Line) value).y1, ((Line) value).x2, ((Line) value).y2);
            }
        };
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
    public void moveConsumersTo(Cell cell) {
        slot.moveConsumersTo(cell);
    }

    @Override
    public void acceptBinding(Binding x) {
        slot.acceptBinding(x);
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
            consumer.next(value());
            return () -> {
                consumers.remove(consumer);
                if(consumers.isEmpty())
                    clean();
            };
        }

        protected void post() {
            consumers.forEach(x -> x.next(value()));
        }

        protected abstract void clean();
    }

    private abstract class ComponentListerPropertyCell extends PropertyCell {
        Object lastValue;

        ComponentListener listener = new ComponentAdapter() {
            {
                lastValue = value();
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
            if(lastValue == null || !lastValue.equals(value()));
                post();
            lastValue = value();
        }
    }

    public Cell property(String name) {
        switch(name) {
            case "x":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value() {
                        return new BigDecimal(getX());
                    }
                };
            case "y":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value() {
                        return new BigDecimal(getY());
                    }
                };
            case "width":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value() {
                        return new BigDecimal(getWidth());
                    }
                };
            case "height":
                return new ComponentListerPropertyCell() {
                    @Override
                    public Object value() {
                        return new BigDecimal(getHeight());
                    }
                };
        }

        return null;
    }
}
