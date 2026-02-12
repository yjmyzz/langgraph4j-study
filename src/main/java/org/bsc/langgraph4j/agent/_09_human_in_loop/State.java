package org.bsc.langgraph4j.agent._09_human_in_loop;

import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Map;
import java.util.Optional;

public class State extends MessagesState<String> {

    public State(Map<String, Object> initData) {
        super( initData  );
    }

    public Optional<String> humanFeedback() {
        return value("human_feedback");
    }

}