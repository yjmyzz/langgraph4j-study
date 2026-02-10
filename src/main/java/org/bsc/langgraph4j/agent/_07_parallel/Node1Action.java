package org.bsc.langgraph4j.agent._07_parallel;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;

public class Node1Action implements NodeAction<AgentState> {

    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-1");
        Thread.sleep(1000);
        return Map.of(
                "myData", "node1-my-value",
                "node1Key", "node1-value",
                //记录开始时间
                "start", System.currentTimeMillis());
    }

}
