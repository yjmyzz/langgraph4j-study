package org.bsc.langgraph4j.agent._09_human_in_loop;


import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.state.StateSnapshot;

import java.io.IOException;
import java.util.Map;

import static java.lang.System.out;
import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


public class WaitUserInputApp {

    public static void main(String[] args) throws Exception {
        AsyncNodeAction<State> step1 = node_async(state -> {
            return Map.of(MessagesState.MESSAGES_STATE, "Step 1");
        });

        AsyncNodeAction<State> humanFeedback = node_async(state -> {
            return Map.of();
        });

        AsyncNodeAction<State> step3 = node_async(state -> {
            return Map.of(MessagesState.MESSAGES_STATE, "Step 3");
        });

        AsyncEdgeAction<State> evalHumanFeedback = edge_async(state -> {
            String feedback = state.humanFeedback().orElseThrow();
            return ("next".equals(feedback) || "back".equals(feedback)) ? feedback : "unknown";
        });

        StateGraph<State> builder = new StateGraph<>(State.SCHEMA, State::new)
                .addNode("step_1", step1)
                .addNode("human_feedback", humanFeedback)
                .addNode("step_3", step3)
                .addEdge(START, "step_1")
                .addEdge("step_1", "human_feedback")
                .addConditionalEdges("human_feedback", evalHumanFeedback,
                        Map.of("back", "step_1", "next", "step_3", "unknown", "human_feedback"))
                .addEdge("step_3", END);

        BaseCheckpointSaver saver = new MemorySaver();

        CompileConfig cc = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptBefore("human_feedback")
                .build();

        CompiledGraph<State> graph = builder.compile(cc);

        out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "Human in the Loop", true).content());

        // Input
        Map<String, Object> initialInput = Map.of("messages", "Step 0");

        // Thread
        RunnableConfig rc = RunnableConfig.builder()
                .threadId("Thread1")
                .build();

        // Run the graph until the first interruption
        for (var event : graph.stream(initialInput, rc)) {
            System.out.println(event.node());
        }

        // 2. Check state at interruption
        StateSnapshot<State> snapshot = graph.getState(rc);

        out.println("=".repeat(50));
        out.println(snapshot);
        out.println("=".repeat(50));

        // 3. Human provides input
        String userInput = "back";  // or "back"

        // 4. Update state with human feedback
        RunnableConfig updateConfig = graph.updateState(rc,
                Map.of("human_feedback", userInput), null);

        // 5. Resume execution
        for (var event : graph.stream(GraphInput.resume(), updateConfig)) {
            System.out.println(event.node());
        }

    }
}
