package org.bsc.langgraph4j.agent._03_springai_agents_handoff;


import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Content;

import java.util.Map;

public class MultiAgentHandoffITest {

    String modelName = "glm-4.6:cloud";

    @Test
    public void testHandoff() throws Exception {
        var handoffExecutor = createGraph().compile();

        System.out.println(handoffExecutor.getGraph(GraphRepresentation.Type.MERMAID, "Agent Handoff", false).content());

        var input = "搜索产品[华为X70手机]并购买";

        var result = handoffExecutor.invoke(Map.of("messages", new UserMessage(input)));

        var response = result.flatMap(MessagesState::lastMessage)
                .map(Content::getText)
                .orElseThrow();

        System.out.println(response);
    }

    public StateGraph<AgentExecutor.State> createGraph() throws GraphStateException {
        AgentMarketplace agentMarketPlace = AgentMarketplace.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .build();

        AgentPayment agentPayment = AgentPayment.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .build();


        return AgentHandoff.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .agent(agentMarketPlace)
                .agent(agentPayment)
                .build();
    }

}
