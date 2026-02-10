package org.bsc.langgraph4j.agent._07_parallel;

import org.apache.logging.log4j.util.Strings;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.concurrent.ExecutorService;
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

        StateGraph<AgentState> graphWithThreadPool = getParallelGraph();
        System.out.println(graphWithThreadPool.getGraph(GraphRepresentation.Type.MERMAID, "parallel Graph", true).content());

        graphWithThreadPool.compile()
                .invoke(Map.of("test", "test-init-value"))
                .ifPresent(c -> {
                    long start = (long) c.data().getOrDefault("start", 0L);
                    System.out.println(c.data());
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) + "ms");
                });


        System.out.println(Strings.repeat("=", 50));

        StateGraph<AgentState> graphNoThreadPool = getParallelGraph();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        RunnableConfig rc = RunnableConfig.builder()
                //从node-1开始并行执行node-2和node-3（使用线程池）
                .addParallelNodeExecutor("node-1", executorService)
                .build();
        graphNoThreadPool.compile()
                .invoke(Map.of("test", "test-init-value"), rc)
                .ifPresent(c -> {
                    long start = (long) c.data().getOrDefault("start", 0L);
                    System.out.println(c.data());
                    //记得关闭线程池
                    executorService.shutdown();
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) + "ms");

                });

        System.out.println(Strings.repeat("=", 50));

        StateGraph<AgentState> graphNoThreadPool2 = getParallelGraph();

        ExecutorService virtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();
        RunnableConfig rc2 = RunnableConfig.builder()
                //从node-1开始并行执行node-2和node-3（使用线程池）
                .addParallelNodeExecutor("node-1", virtualThreadPerTaskExecutor)
                .build();
        graphNoThreadPool2.compile()
                .invoke(Map.of("test", "test-init-value"), rc2)
                .ifPresent(c -> {
                    long start = (long) c.data().getOrDefault("start", 0L);
                    System.out.println(c.data());
                    long end = System.currentTimeMillis();
                    System.out.println((end - start) + "ms");
                });

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