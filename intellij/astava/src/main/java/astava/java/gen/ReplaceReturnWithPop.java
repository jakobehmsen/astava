package astava.java.gen;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Hashtable;
import java.util.Map;

public class ReplaceReturnWithPop extends InstructionAdapter {
    public ReplaceReturnWithPop(GeneratorAdapter generator) {
        super(Opcodes.ASM5, generator);
    }

    private Label returnLabel;

    @Override
    public void areturn(Type type) {
        if(returnLabel == null) {
            returnLabel = ((GeneratorAdapter)mv).newLabel();
        }

        if(!type.equals(Type.VOID_TYPE))
            ((GeneratorAdapter)mv).pop();
        ((GeneratorAdapter)mv).visitJumpInsn(Opcodes.GOTO, returnLabel);
    }

    public void visitReturn() {
        if(returnLabel != null)
            ((GeneratorAdapter)mv).visitLabel(returnLabel);
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
