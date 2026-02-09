package org.bsc.langgraph4j.agent._11_hook;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;

import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.lang.System.out;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Hook 示例：Node Hook 与 Edge Hook 的触发时机。
 * <p>
 * 重要：Edge 相关 Hook（addBeforeCallEdgeHook / addAfterCallEdgeHook / addWrapCallEdgeHook）
 * 只会在「条件边」被求值时触发。即仅当图中存在 addConditionalEdges(...) 且运行时执行到
 * 该条件边、去「调用」边的 Action 时，才会执行 Edge Hook。
 * 纯静态边 addEdge(A, B) 只是图上的连线，运行时不会「调用」任何边逻辑，因此不会触发 Edge Hook。
 * <p>
 * 本类先跑纯静态边图（仅 Node Hook 会打印），再跑带条件边的图（Node + Edge Hook 都会打印）。
 *
 * @author 菩提树下的杨过(yjmyzz.cnblogs.com)
 * @since 1.0
 */
public class HookSampleApplication {

    public static void main(String[] args) throws GraphStateException {
        runSequenceGraphWithOnlyStaticEdges();
        out.println("\n========== 下面使用带条件边的图，Edge Hook 会执行 ==========");
        runGraphWithConditionalEdge();
    }

    /**
     * 纯静态边：只有 Node Hook 会执行，Edge Hook 不会执行
     */
    private static void runSequenceGraphWithOnlyStaticEdges() throws GraphStateException {
        StateGraph<AgentState> sequenceGraph = getSequenceGraph();

        sequenceGraph.addBeforeCallNodeHook((String node, AgentState data, RunnableConfig config) -> {
            out.println("Before calling node: " + node + ", data: " + data.data());
            return CompletableFuture.completedFuture(data.data());
        });

        sequenceGraph.addAfterCallNodeHook((String node, AgentState data, RunnableConfig config, Map<String, Object> lastResult) -> {
            out.println("After calling node: " + node + ", data: " + data.data() + ", lastResult: " + lastResult);
            return CompletableFuture.completedFuture(lastResult);
        });

        sequenceGraph.addWrapCallNodeHook((String node, AgentState data, RunnableConfig config, AsyncNodeActionWithConfig<AgentState> action) -> {
            out.println("Wrap calling node: " + node + ", data: " + data.data());
            long start = System.currentTimeMillis();
            return action.apply(data, config).whenComplete((result, error) -> {
                var ms = System.currentTimeMillis() - start;
                out.println(String.format("node '%s' took %d ms", node, ms));
            });
        });

        sequenceGraph.addBeforeCallEdgeHook((String sourceId, AgentState state, RunnableConfig config) -> {
            out.println("Before calling edge: " + sourceId);
            return CompletableFuture.completedFuture(new Command(state.data()));
        });

        sequenceGraph.addAfterCallEdgeHook((String sourceId, AgentState state, RunnableConfig config, Command lastResult) -> {
            out.println("After calling edge: " + sourceId);
            return CompletableFuture.completedFuture(lastResult);
        });

        sequenceGraph.addWrapCallEdgeHook((String sourceId, AgentState state, RunnableConfig config, AsyncCommandAction<AgentState> action) -> {
            out.println("Wrap calling edge: " + sourceId);
            long start = System.currentTimeMillis();
            return action.apply(state, config).whenComplete((result, error) -> {
                var ms = System.currentTimeMillis() - start;
                out.println(String.format("source-node '%s' took %d ms", sourceId, ms));
            });

        });

        out.println(sequenceGraph.getGraph(GraphRepresentation.Type.MERMAID, "Conditional Graph with hook", true).content());

        sequenceGraph.compile().invoke(Map.of("test", "test-init-value")).ifPresent(c -> {
            System.out.println(c.data());
        });
    }

    /**
     * 带条件边：从 node-1 经条件边到 node-2，会触发 Edge Hook
     */
    private static void runGraphWithConditionalEdge() throws GraphStateException {
        StateGraph<AgentState> graph = getGraphWithConditionalEdge();

        graph.addBeforeCallNodeHook((String node, AgentState data, RunnableConfig config) -> {
            out.println("Before calling node: " + node + ", data: " + data.data());
            return CompletableFuture.completedFuture(data.data());
        });
        graph.addAfterCallNodeHook((String node, AgentState data, RunnableConfig config, Map<String, Object> lastResult) -> {
            out.println("After calling node: " + node + ", data: " + data.data() + ", lastResult: " + lastResult);
            return CompletableFuture.completedFuture(lastResult);
        });
        graph.addWrapCallNodeHook((String node, AgentState data, RunnableConfig config, AsyncNodeActionWithConfig<AgentState> action) -> {
            out.println("Wrap calling node: " + node + ", data: " + data.data());
            long start = System.currentTimeMillis();
            return action.apply(data, config).whenComplete((result, error) -> {
                var ms = System.currentTimeMillis() - start;
                out.println(String.format("node '%s' took %d ms", node, ms));
            });
        });

        graph.addBeforeCallEdgeHook((String sourceId, AgentState state, RunnableConfig config) -> {
            out.println("Before calling edge: " + sourceId);
            return CompletableFuture.completedFuture(new Command(state.data()));
        });
        graph.addAfterCallEdgeHook((String sourceId, AgentState state, RunnableConfig config, Command lastResult) -> {
            out.println("After calling edge: " + sourceId);
            return CompletableFuture.completedFuture(lastResult);
        });
        graph.addWrapCallEdgeHook((String sourceId, AgentState state, RunnableConfig config, AsyncCommandAction<AgentState> action) -> {
            out.println("Wrap calling edge: " + sourceId);
            long start = System.currentTimeMillis();
            return action.apply(state, config).whenComplete((result, error) -> {
                var ms = System.currentTimeMillis() - start;
                out.println(String.format("source-node '%s' took %d ms", sourceId, ms));
            });
        });

        out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "Sequence Graph with hook", true).content());

        graph.compile().invoke(Map.of("test", "test-init-value")).ifPresent(c -> System.out.println(c.data()));
    }

    public static StateGraph<AgentState> getSequenceGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-2", GraphDefinition.END);
    }

    /**
     * 含一条条件边：node-1 通过条件边到 node-2，用于演示 Edge Hook 触发
     */
    public static StateGraph<AgentState> getGraphWithConditionalEdge() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addEdge(GraphDefinition.START, "node-1")
                .addConditionalEdges("node-1", state -> CompletableFuture.completedFuture("toNode2"), Map.of("toNode2", "node-2"))
                .addEdge("node-2", GraphDefinition.END);
    }
}
