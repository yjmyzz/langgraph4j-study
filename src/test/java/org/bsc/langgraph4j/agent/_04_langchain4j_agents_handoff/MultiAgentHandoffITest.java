package org.bsc.langgraph4j.agent._04_langchain4j_agents_handoff;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.GraphRepresentation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class MultiAgentHandoffITest {

    enum AiModel {

        OPENAI_GPT_4O_MINI(OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .supportedCapabilities(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA))
//                .logRequests(true)
//                .logResponses(true)
//                .maxRetries(2)
                .temperature(0.1)
                .build()),
        OLLAMA_CLOUD(OllamaChatModel.builder()
//                .modelName("glm-4.6:cloud")
//                .modelName("deepseek-v3.1:671b-cloud")
                .modelName("glm-4.6:cloud")
                .baseUrl("http://localhost:11434")
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .logResponses(true)
//                .maxRetries(2)
                .temperature(0.1)
                .build());

        public final ChatModel model;

        AiModel(ChatModel model) {
            this.model = model;
        }
    }


    @Test
    public void testHandoff() throws Exception {

        var agentMarketplace = AgentMarketplace.builder()
                .chatModel(AiModel.OLLAMA_CLOUD.model)
                .build();

        var agentPayment = AgentPayment.builder()
                .chatModel(AiModel.OLLAMA_CLOUD.model)
                .build();

        var handoffExecutor = AgentHandoff.builder()
                .chatModel(AiModel.OLLAMA_CLOUD.model)
                .agent(agentMarketplace)
                .agent(agentPayment)
                .build()
                .compile();

        System.out.println(handoffExecutor.getGraph(GraphRepresentation.Type.MERMAID, "Agent Handoff", false).content());

        var input = "搜索产品[华为X70手机]并购买";

        var result = handoffExecutor.invoke(Map.of("messages", UserMessage.from(input)));

        System.out.println(result);

    }
}