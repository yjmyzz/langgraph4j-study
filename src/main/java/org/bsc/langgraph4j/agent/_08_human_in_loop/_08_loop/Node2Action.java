package org.bsc.langgraph4j.agent._08_human_in_loop._08_loop;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;

public class Node2Action implements NodeAction<AgentState> {
    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-2");
        long nextCount = getLoopCount(state) + 1;
        return Map.of(
                "myData", "node2-my-value",
                "node2Key", "node2-value",
                "loopCount", nextCount);
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
