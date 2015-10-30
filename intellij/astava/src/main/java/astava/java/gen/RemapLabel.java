package astava.java.gen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Hashtable;
import java.util.Map;

public class RemapLabel extends InstructionAdapter {
    public RemapLabel(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    private Map<Label, Label> origToNewLabelMap = new Hashtable<>();

    private Label getNewLabel(Label origLabel) {
        return origToNewLabelMap.computeIfAbsent(origLabel, k -> new Label());
    }

    @Override
    public void visitLabel(Label label) {
        Label newLabel = getNewLabel(label);
        super.visitLabel(newLabel);
    }

    @Override
    public void visitJumpInsn(int i, Label label) {
        Label newLabel = getNewLabel(label);
        super.visitJumpInsn(i, newLabel);
    }
}
