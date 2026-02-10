package org.bsc.langgraph4j.agent._08_loop;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * LoopGraphApplication 演示了如何使用 LangGraph4j 构建一个带有循环逻辑的图应用。
 * 
 * 该应用创建了一个简单的循环图，其中包含两个节点：
 * - node-1: 执行主要逻辑并检查循环计数
 * - node-2: 辅助处理节点
 * 
 * 循环逻辑通过条件边实现，当循环计数达到预设的最大值(MAX_LOOP_ITERATIONS=3)时退出循环。
 * 
 * 图的执行流程：
 * START -> node-1 -> (条件判断) -> node-2 -> node-1 -> ... -> END
 * 
 * 主要功能：
 * 1. 展示图的Mermaid可视化表示
 * 2. 执行图并输出最终状态
 * 3. 演示循环计数的管理和条件边的使用
 *
 * @author 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
public class LoopGraphApplication {

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> sequenceGraph = getLoopGraph();

        System.out.println(sequenceGraph.getGraph(GraphRepresentation.Type.MERMAID, "loop Graph", true).content());

        sequenceGraph.compile().invoke(Map.of("loopCount", 0L)).ifPresent(c -> {
            System.out.println(c.data());
        });

    }

    private static final int MAX_LOOP_ITERATIONS = 3;

    public static StateGraph<AgentState> getLoopGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-2", "node-1")
                //循环的跳出条件比较简单，3次后退出，这里就不单独定义EdgeAction类了，用lambda表达式
                .addConditionalEdges("node-1", state -> {
                    long count = getLoopCount(state);
                    System.out.println("loop Count: " + count);
                    if (count >= MAX_LOOP_ITERATIONS) {
                        return CompletableFuture.completedFuture("exit");
                    }
                    return CompletableFuture.completedFuture("continue");
                }, Map.of(
                        "exit", GraphDefinition.END,
                        "continue", "node-2"));
    }

    private static long getLoopCount(AgentState state) {
        Optional<Object> loopCount = state.value("loopCount");
        if (loopCount.isEmpty()) {
            return 0L;
        }
        Object v = loopCount.get();
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(v.toString());
    }


}
