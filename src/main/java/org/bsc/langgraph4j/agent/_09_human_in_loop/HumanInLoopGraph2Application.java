package org.bsc.langgraph4j.agent._09_human_in_loop;

import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.StateSnapshot;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


public class HumanInLoopGraph2Application {

    private static final String LOOP_COUNT_KEY = "loopCount";
    private static final Scanner CONSOLE_SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        StateGraph<AgentState> graph = getLoopGraph();

        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "human-in-loop Graph", true).content());

        CompileConfig compileConfig = CompileConfig.builder()
                .interruptBefore("node-2")
                .checkpointSaver(new MemorySaver())
                .build();

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("thread-1")
                .build();

        CompiledGraph<AgentState> workflow = graph.compile(compileConfig);
        workflow.invoke(Map.of(), runnableConfig).ifPresent(c -> {
            System.out.println(c.data());
        });

        StateSnapshot<AgentState> snapshot = workflow.getState(runnableConfig);
        if (snapshot != null) {
            RunnableConfig updateConfig = workflow.updateState(runnableConfig,
                    Map.of("loopCount", snapshot.state().value("loopCount").orElse(0)), null);
            for (var event : workflow.stream(GraphInput.resume(), updateConfig)) {
                System.out.println(event.node());
            }
        }
    }

    public static StateGraph<AgentState> getLoopGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(state -> {
                    int loopCount = (int) state.value(LOOP_COUNT_KEY).orElse(0);
                    System.out.println("node-1: loopCount = " + loopCount);
                    return Map.of(LOOP_COUNT_KEY, loopCount + 1);
                }))
                .addNode("node-2", node_async(state -> {
                    int loopCount = (int) state.value(LOOP_COUNT_KEY).orElse(0);
                    System.out.println("node-2: loopCount = " + loopCount);
                    return Map.of();
                }))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-2", "node-1")
                .addConditionalEdges("node-1", state -> CompletableFuture.supplyAsync(HumanInLoopGraph2Application::waitForHumanDecision),
                        Map.of(
                                "exit", GraphDefinition.END,
                                "next", "node-2",
                                "back", "node-1"));
    }


    private static String waitForHumanDecision() {
        while (true) {
            System.out.print("请输入 N(next) 继续到 node-2，B(back) 退回到 node-1，或 Q(Quit) 结束 [N/B/Q]: ");
            if (!CONSOLE_SCANNER.hasNextLine()) {
                return "exit";
            }
            String input = CONSOLE_SCANNER.nextLine();
            if (input == null) {
                continue;
            }
            String trimmed = input.trim().toUpperCase();
            if ("N".equals(trimmed)) {
                return "next";
            }
            if ("B".equals(trimmed)) {
                return "back";
            }
            if ("Q".equals(trimmed)) {
                return "exit";
            }
            System.out.println("无效输入，请只输入 N 或 B 或 Q");
        }
    }
}
