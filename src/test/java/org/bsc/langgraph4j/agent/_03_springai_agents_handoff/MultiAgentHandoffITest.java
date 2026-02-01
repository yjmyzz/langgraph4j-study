package org.bsc.langgraph4j.agent._03_springai_agents_handoff;


import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Content;

import java.util.Map;

public class MultiAgentHandoffITest {

    @Test
    public void testHandoff() throws Exception {

        String modelName = "glm-4.6:cloud";

        var agentMarketPlace = AgentMarketplace.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .build();

        var agentPayment = AgentPayment.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .build();


        var handoffExecutor = AgentHandoff.builder()
                .chatModel(AiModel.OLLAMA.chtModel(modelName))
                .agent(agentMarketPlace)
                .agent(agentPayment)
                .build()
                .compile();

        System.out.println(handoffExecutor.getGraph(GraphRepresentation.Type.MERMAID, "Agent Handoff", false).content());

        var input = "搜索产品[华为X70手机]并购买";

        var result = handoffExecutor.invoke(Map.of("messages", new UserMessage(input)));

        var response = result.flatMap(MessagesState::lastMessage)
                .map(Content::getText)
                .orElseThrow();

        System.out.println(response);
    }
}
