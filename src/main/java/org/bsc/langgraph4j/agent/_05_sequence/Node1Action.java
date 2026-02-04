package org.bsc.langgraph4j.agent._05_sequence;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class Node1Action implements NodeAction<AgentState> {
    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-1");
        return Map.of("myData", "node1-my-value",
                "node1Key", "node1-value");
    }
}
