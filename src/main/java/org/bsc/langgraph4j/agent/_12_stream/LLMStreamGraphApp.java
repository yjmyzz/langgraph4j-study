package org.bsc.langgraph4j.agent._12_stream;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.langchain4j.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.langchain4j.serializer.std.LC4jStateSerializer;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.bsc.langgraph4j.streaming.StreamingOutput;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.prebuilt.MessagesState.MESSAGES_STATE;

public class LLMStreamGraphApp {

    public static void main(String[] args) throws GraphStateException {

        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName("qwen3:1.7b")
                .build();

        //定义Agent节点
        NodeAction<MessagesState<ChatMessage>> callModel = state -> {
            StreamingChatGenerator<MessagesState<ChatMessage>> generator = StreamingChatGenerator.<MessagesState<ChatMessage>>builder()
                    .mapResult(response -> Map.of(MESSAGES_STATE, response.aiMessage()))
                    .startingNode("agent")
                    .startingState(state)
                    .build();

            ChatRequest request = ChatRequest.builder()
                    .messages(state.messages())
                    .build();

            model.chat(request, generator.handler());
            //注：key名可以改成其它，不一定非要是“_streaming_messages”
            return Map.of("_streaming_messages", generator);
        };


        //定义图
        CompiledGraph<MessagesState<ChatMessage>> graph = new MessagesStateGraph<>(new LC4jStateSerializer<MessagesState<ChatMessage>>(MessagesState::new))
                .addNode("agent", node_async(callModel))
                .addEdge(START, "agent")
                .addEdge("agent", END)
                .compile();

        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "LLM Stream Graph", true).content());

        //流式执行
        AsyncGenerator<NodeOutput<MessagesState<ChatMessage>>> stream = graph.stream(Map.of(MESSAGES_STATE, UserMessage.from("李清照的成名作有哪些？")));

        //输出流式结果
        for (NodeOutput<MessagesState<ChatMessage>> out : stream) {
            if (out instanceof StreamingOutput<?> streamingOut) {
                System.out.print(streamingOut.chunk());
            }
        }

    }
}
