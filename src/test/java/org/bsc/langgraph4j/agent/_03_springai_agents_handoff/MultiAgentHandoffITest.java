package org.bsc.langgraph4j.agent._03_springai_agents_handoff;


import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Content;

import java.util.Map;

public class MultiAgentHandoffITest {

    @Test
    public void testHandoff() throws Exception {

        String modelName = "minimax-m2:cloud";

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

        var input = "search for product 'X' and purchase it with IBAN US82WEST1234567890123456";

        var result = handoffExecutor.invoke(Map.of("messages", new UserMessage(input)));

        var response = result.flatMap(MessagesState::lastMessage)
                .map(Content::getText)
                .orElseThrow();

        System.out.println(response);
    }
}
