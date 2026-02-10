package org.bsc.langgraph4j.agent._06_conditional;

import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Optional;
import java.util.Set;

/**
 * 条件边：根据 state 中的 nextNode 选择下一节点。
 * 仅当返回值在合法节点集合内时才使用，否则回退到默认节点，避免框架因非法 key 抛错。
 * @author junmingyang
 */
public class RoutingEdgeAction implements EdgeAction<AgentState> {

    private static final String KEY_NEXT_NODE = "nextNode";

    private final Set<String> allowedNodes;
    private final String defaultNode;

    public RoutingEdgeAction(Set<String> allowedNodes, String defaultNode) {
        this.allowedNodes = Set.copyOf(allowedNodes);
        if (!this.allowedNodes.contains(defaultNode)) {
            throw new IllegalArgumentException("defaultNode must be in allowedNodes: " + defaultNode);
        }
        this.defaultNode = defaultNode;
    }

    @Override
    public String apply(AgentState state) throws Exception {
        //根据上1个节点的状态值，执行路由
        System.out.println("current Edge: routing edge");
        String next = resolveNextNode(state);
        if (allowedNodes.contains(next)) {
            return next;
        }
        return defaultNode;
    }

    private String resolveNextNode(AgentState state) {
        Optional<Object> nextOpt = state.value(KEY_NEXT_NODE);
        if (nextOpt.isEmpty()) {
            return defaultNode;
        }
        Object v = nextOpt.get();
        return v.toString();
    }
}
