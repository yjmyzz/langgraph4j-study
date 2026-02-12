package org.bsc.langgraph4j.agent._12_stream;

import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import java.util.Map;

/**
 * @author junmingyang
 */
public class ChatNode implements NodeAction<MessagesState<String>> {

    private final String message;

    public ChatNode(String message) {
        this.message = message;
    }

    @Override
    public Map<String, Object> apply(MessagesState<String> state) throws Exception {
//        System.out.println("current messages => " + state.messages());
        //模拟节点执行耗时
        Thread.sleep(2000);
        return Map.of(MessagesState.MESSAGES_STATE, this.message);
    }
}
