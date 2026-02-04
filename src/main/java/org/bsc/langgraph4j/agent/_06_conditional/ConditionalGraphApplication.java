package org.bsc.langgraph4j.agent._06_conditional;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Set;

import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 条件图示例：start -> node-1 -> 根据 node-1 写回的 nextNode 分支到 node-2 / node-3 / node-4 -> END。
 * 可通过初始状态 "routeTo"（"node-2"|"node-3"|"node-4"）指定分支，未指定时默认走 node-2。
 *
 * @author junmingyang
 */
public class ConditionalGraphApplication {

    private static final Set<String> CONDITIONAL_TARGETS = Set.of("2", "3");
    private static final String DEFAULT_TARGET = "2";

    public static void main(String[] args) throws GraphStateException {


        StateGraph<AgentState> conditionalGraph = getConditionalGraph();

        System.out.println(conditionalGraph.getGraph(GraphRepresentation.Type.MERMAID, "conditional Graph", false).content());

        try {
            conditionalGraph.compile()
                    .invoke(Map.of("test", "test-init-value", "routeTo", "node-4"))
                    .ifPresent(c -> System.out.println(c.data()));
        } catch (GraphStateException e) {
            System.err.println("Graph execution failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static StateGraph<AgentState> getConditionalGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addNode("node-3", node_async(new Node3Action()))
                .addEdge(START, "node-1")
                .addConditionalEdges("node-1",
                        edge_async(new RoutingEdgeAction(CONDITIONAL_TARGETS, DEFAULT_TARGET)),
                        // 如果返回2，则跳到node-2，如果返回3，则跳到node-3
                        Map.of("2", "node-2",
                                "3", "node-3"))
                .addEdge("node-2", GraphDefinition.END)
                .addEdge("node-3", GraphDefinition.END);
    }
}