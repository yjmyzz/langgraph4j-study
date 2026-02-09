package org.bsc.langgraph4j.agent._07_parallel;

import org.apache.logging.log4j.util.Strings;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        StateGraph<AgentState> conditionalGraph1 = getParallelGraph();

        System.out.println(conditionalGraph1.getGraph(GraphRepresentation.Type.MERMAID, "parallel Graph", true).content());

        long start = System.currentTimeMillis();
        try {
            RunnableConfig rc = RunnableConfig.builder()
                    .addParallelNodeExecutor("node-1", Executors.newFixedThreadPool(4))
                    .build();
            conditionalGraph1.compile()
                    .invoke(Map.of("test", "test-init-value"), rc)
                    .ifPresent(c -> System.out.println(c.data()));
        } catch (GraphStateException e) {
            System.err.println("Graph execution failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println((end - start) + "ms");
        }

        System.out.println(Strings.repeat("=", 50));

        StateGraph<AgentState> conditionalGraph2 = getParallelGraph();
        start = System.currentTimeMillis();
        try {
            conditionalGraph2.compile()
                    .invoke(Map.of("test", "test-init-value"))
                    .ifPresent(c -> System.out.println(c.data()));
        } catch (GraphStateException e) {
            System.err.println("Graph execution failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println((end - start) + "ms");
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