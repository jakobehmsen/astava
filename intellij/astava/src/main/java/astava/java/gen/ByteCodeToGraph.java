package astava.java.gen;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

public class ByteCodeToGraph {
    public static DirectedGraph<AbstractInsnNode, Object> convert(InsnList instructions) {
        DirectedGraph<AbstractInsnNode, Object> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(DefaultEdge.class));

        if(instructions.size() > 0)
            convert(instructions.getFirst(), graph);

        return graph;
    }

    private static void convert(AbstractInsnNode previous, DirectedGraph<AbstractInsnNode, Object> graph) {
        graph.addVertex(previous);

        Hashtable<Label, AbstractInsnNode> labelToBranchVertexMap = new Hashtable<>();
        Hashtable<Label, AbstractInsnNode> labelToVertexMap = new Hashtable<>();
        ArrayList<Runnable> postProcessors = new ArrayList<>();
        Stack<AbstractInsnNode> branchStack = new Stack<>();

        while(true) {
            AbstractInsnNode current = previous.getNext();

            if(current != null) {
                graph.addVertex(current);

                graph.addEdge(previous, current);

                switch (current.getType()) {
                    case AbstractInsnNode.JUMP_INSN:
                        Label label = ((JumpInsnNode) current).label.getLabel();

                        if(current.getOpcode() == Opcodes.GOTO) {
                            branchStack.pop();

                            //AbstractInsnNode branchVertex = labelToBranchVertexMap.get(((LabelNode) current).getLabel());
                            AbstractInsnNode sourceVertex = current;

                            if(labelToVertexMap.containsKey(label)) {
                                // Jump backward / loop
                            } else {
                                // Jump forward

                                postProcessors.add(() -> {
                                    AbstractInsnNode targetVertex = labelToBranchVertexMap.get(label);
                                    graph.addEdge(sourceVertex, targetVertex);
                                });
                            }
                        } else if(current.getOpcode() >= Opcodes.IFEQ && current.getOpcode() <= Opcodes.IF_ACMPNE) {
                            // What if there are multiple branches with the same label?
                            // Index branch vertex
                            labelToBranchVertexMap.put(label, current);

                            branchStack.push(current);
                            branchStack.push(current);
                        } else {
                            // How to handle this kind of instruction?
                        }

                        break;
                    case AbstractInsnNode.LABEL: {
                        labelToVertexMap.put(((LabelNode) current).getLabel(), current);

                        AbstractInsnNode branchVertex = labelToBranchVertexMap.get(((LabelNode) current).getLabel());
                        if (branchVertex != null) {
                            branchStack.pop();
                            graph.addEdge(branchVertex, current);
                        }

                        break;
                    } default:
                        if(current.getOpcode() == Opcodes.RETURN || current.getOpcode() == Opcodes.ARETURN) {
                            if(branchStack.size() > 0) {
                                AbstractInsnNode branchVertex = branchStack.pop();
                                current = branchVertex;
                            }
                        }

                        break;
                }

                previous = current;
            } else
                break;
        }

        postProcessors.forEach(x -> x.run());
    }
}
