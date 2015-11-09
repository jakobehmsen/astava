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

        Hashtable<Label, AbstractInsnNode> branchLabelToVertexMap = new Hashtable<>();

        while(true) {
            AbstractInsnNode current = previous.getNext();

            if(current != null) {
                graph.addVertex(current);

                graph.addEdge(previous, current);

                boolean jumpedBack = false;

                while(jumpedBack) {
                    switch (current.getType()) {
                        case AbstractInsnNode.JUMP_INSN:
                            // What if there are multiple branches with the same label?
                            // Index branch vertex
                            branchLabelToVertexMap.put(((JumpInsnNode) current).label.getLabel(), current);

                            break;
                        case AbstractInsnNode.LABEL:
                            AbstractInsnNode branchVertex = branchLabelToVertexMap.get(((LabelNode) current).getLabel());
                            if (branchVertex != null) {
                                // "Jump" back to branch vertex, from which the F route was started, and now
                                // start the T route
                                current = branchVertex;
                                jumpedBack = true;
                            }

                            break;
                    }
                }

                previous = current;
            } else
                break;
        }
    }
}
