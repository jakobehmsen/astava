package astava.java.gen;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

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
}
