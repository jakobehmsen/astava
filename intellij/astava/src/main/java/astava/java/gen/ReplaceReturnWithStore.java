package astava.java.gen;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

public class ReplaceReturnWithStore extends InstructionAdapter {
    public ReplaceReturnWithStore(GeneratorAdapter generator) {
        super(Opcodes.ASM5, generator);
    }

    private Label returnLabel;
    private int returnVar = -1;

    @Override
    public void visitCode() {
        returnLabel = null;
        returnVar = -1;
    }

    @Override
    public void areturn(Type type) {
        if(returnVar == -1) {
            returnLabel = ((GeneratorAdapter)mv).newLabel();
            returnVar = ((GeneratorAdapter)mv).newLocal(type);
        }

        ((GeneratorAdapter)mv).storeLocal(returnVar);
        ((GeneratorAdapter)mv).visitJumpInsn(Opcodes.GOTO, returnLabel);
    }

    public void returnStart() {
        if(returnVar != -1)
            ((GeneratorAdapter)mv).visitLabel(returnLabel);
    }

    public void returnEnd() {
        if(returnVar != -1) {
            ((GeneratorAdapter)mv).loadLocal(returnVar);
            ((GeneratorAdapter)mv).returnValue();
        }
    }
}
