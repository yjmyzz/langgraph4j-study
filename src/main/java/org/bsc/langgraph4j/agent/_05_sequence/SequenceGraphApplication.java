package org.bsc.langgraph4j.agent._05_sequence;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * SequenceGraphApplication 类演示了如何使用 LangGraph4j 构建和执行一个简单的序列图。
 * 该应用程序创建了一个包含两个节点的序列图，节点按顺序执行：
 * START -> node-1 -> node-2 -> END
 * 
 * 主要功能包括：
 * 1. 构建序列图结构
 * 2. 生成图的 Mermaid 表示
 * 3. 执行图并输出结果
 * 
 * @author 菩提树下的杨过(yjmyzz.cnblogs.com)
 * @since 1.0
 */
public class SequenceGraphApplication {

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> sequenceGraph = getSequenceGraph();

        System.out.println(sequenceGraph.getGraph(GraphRepresentation.Type.MERMAID, "Sequence Graph", true).content());

        sequenceGraph.compile().invoke(Map.of("test", "test-init-value")).ifPresent(c -> {
            System.out.println(c.data());
        });

    }

    public static StateGraph<AgentState> getSequenceGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-2", GraphDefinition.END);
    }


}
