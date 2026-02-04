package org.bsc.langgraph4j.agent._06_conditional;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Optional;

/**
 * 条件图专用：根据当前状态决定下一节点（通过 nextNode 供 RoutingEdgeAction 使用）。
 * 若初始状态包含 "routeTo"（值为 "node-2"|"node-3"|"node-4"）则使用该路由，否则默认 "node-2"。
 *
 * @author junmingyang
 */
public class Node1Action implements NodeAction<AgentState> {

    private static final String KEY_NEXT_NODE = "nextNode";
    private static final String DEFAULT_NEXT = "3";

    @Override
    public Map<String, Object> apply(AgentState state) throws Exception {
        System.out.println("current Node: node-1");
        String next = resolveNextNode(state);
        return Map.of(
                "myData", "node1-my-value",
                "node1Key", "node1-value",
                KEY_NEXT_NODE, next);
    }

    private String resolveNextNode(AgentState state) {
        Optional<Object> routeTo = state.value(KEY_NEXT_NODE);
        if (routeTo.isEmpty()) {
            return DEFAULT_NEXT;
        }
        Object v = routeTo.get();
        String s = v.toString();
        if ("2".equals(s) || "3".equals(s)) {
            return s;
        }
        return DEFAULT_NEXT;
    }
}
