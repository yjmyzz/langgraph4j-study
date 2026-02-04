package org.bsc.langgraph4j.agent._05_sequence;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class SequenceGraphApplication {

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> sequenceGraph = getSequenceGraph();

        System.out.println(sequenceGraph.getGraph(GraphRepresentation.Type.MERMAID, "Sequence Graph", false).content());

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
