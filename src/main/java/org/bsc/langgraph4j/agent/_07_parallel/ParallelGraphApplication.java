package org.bsc.langgraph4j.agent._07_parallel;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Set;

import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * 并行图应用示例
 * <p>
 * 本类演示了如何构建和执行一个具有并行分支的图结构。
 * 图中包含三个节点：node-1、node-2 和 node-3，
 * 其中 node-2 和 node-3 在 node-1 执行完成后并行执行。
 * <p>
 * 图结构：
 * START -> node-1 -> node-2 -> END
 * |
 * v
 * node-3 -> END
 *
 * @author 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
public class ParallelGraphApplication {


    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> conditionalGraph = getParallelGraph();

        System.out.println(conditionalGraph.getGraph(GraphRepresentation.Type.MERMAID, "parallel Graph", false).content());

        try {
            conditionalGraph.compile()
                    .invoke(Map.of("test", "test-init-value"))
                    .ifPresent(c -> System.out.println(c.data()));
        } catch (GraphStateException e) {
            System.err.println("Graph execution failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static StateGraph<AgentState> getParallelGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addNode("node-3", node_async(new Node3Action()))
                .addEdge(START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-1", "node-3")
                .addEdge("node-2", GraphDefinition.END)
                .addEdge("node-3", GraphDefinition.END);
    }
}