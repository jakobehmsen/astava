package astava.java.gen;

import org.jgrapht.DirectedGraph;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by jakob on 09-11-15.
 */
public class ByteCodeToGraphTest {
    private InsnList getInstructions(Object testCase) {
        Class<?> c = testCase.getClass();
        InputStream resource = c.getResourceAsStream(c.getName().substring(c.getName().lastIndexOf('.') + 1) + ".class");

        ClassReader cr = null;
        try {
            cr = new ClassReader(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        cr.accept(classNode, org.objectweb.asm.ClassReader.EXPAND_FRAMES);

        String byteCodeMethodName = "byteCode";
        MethodNode methodNode = ((List<MethodNode>)classNode.methods).stream().filter(y -> y.name.equals(byteCodeMethodName)).findFirst().get();

        return methodNode.instructions;
    }

    @Test
    public void testConvert() throws Exception {
        InsnList instructions = getInstructions(new Object() {
            private int i;

            public void byteCode() {
                if(i == 0) {
                    i++;
                } else {
                    i--;
                }
            }
        });
        DirectedGraph<AbstractInsnNode, Object> graph = ByteCodeToGraph.convert(instructions);

        graph.outgoingEdgesOf(instructions.getFirst());
    }

    private void print(DirectedGraph<AbstractInsnNode, Object> graph, AbstractInsnNode start) {
        Set<Object> outgoing = graph.outgoingEdgesOf(start);
        if(outgoing.size() == 1) {

        } else if(outgoing.size() == 2) {

        } else if(outgoing.size() == 0) {

        } else {

        }
    }
}