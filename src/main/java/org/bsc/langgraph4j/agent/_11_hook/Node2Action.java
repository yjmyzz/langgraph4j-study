package org.bsc.langgraph4j.agent._11_hook;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class Node2Action implements NodeAction<AgentState> {
    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-2");
        //模拟节点耗时
        Thread.sleep(2000);
        return Map.of("myData", "node2-my-value",
                "node2Key", "node2-value");
    }
}
