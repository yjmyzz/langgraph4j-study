package org.bsc.langgraph4j.agent._12_stream;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.bsc.langgraph4j.langchain4j.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.streaming.StreamingOutput;

import java.util.Map;

public class LLMStreamApp {

    public static void main(String[] args) {

        StreamingChatGenerator<AgentState> generator = StreamingChatGenerator.builder()
                .mapResult(r -> Map.of("content", r.aiMessage().text()))
                .build();

        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("qwen3:1.7b")
                .build();

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("李清照的成名作有哪些？"))
                .build();

        model.chat(request, generator.handler());

        for (StreamingOutput<AgentState> output : generator) {
            System.out.print(output.chunk());
        }
    }
}
