package org.bsc.langgraph4j.agent._07_parallel;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class Node3Action implements NodeAction<AgentState> {
    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-3");
        Thread.sleep(1000);
        return Map.of("myData", "node3-my-value",
                "node3Key", "node3-value");
    }
}
