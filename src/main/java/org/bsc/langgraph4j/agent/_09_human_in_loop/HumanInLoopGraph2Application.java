package org.bsc.langgraph4j.agent._09_human_in_loop;

import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.StateSnapshot;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 人机协同（Human-in-the-Loop）示例：在图执行到指定节点前中断，等待控制台输入后再恢复。
 * <p>
 * 图结构：START → node-1 → [条件边] → node-2 / node-reset / node-1 / END
 * <ul>
 *   <li>node-1：递增 loopCount，然后根据用户输入决定下一跳</li>
 *   <li>node-2、node-reset：空节点，执行后回到 node-1</li>
 *   <li>条件边由 {@link #waitForHumanDecision()} 驱动：N(next)→node-2，B(back)→node-1，R(Reset)→node-reset，Q(Quit)→END</li>
 * </ul>
 * 通过 {@code interruptBefore("node-2")} 与 {@code interruptBefore("node-reset")} 在进入下一节点前暂停，
 * 使用 checkpoint 保存状态，再通过 {@link GraphInput#resume()} 恢复执行，实现“按 N 一直循环”等人机交互。
 *
 * @see org.bsc.langgraph4j.CompiledGraph#stream
 * @see org.bsc.langgraph4j.CompileConfig.Builder#interruptBefore
 */
public class HumanInLoopGraph2Application {

    /**
     * 状态中的循环计数字段名
     */
    private static final String LOOP_COUNT_KEY = "loopCount";
    /**
     * 控制台输入扫描器，供 waitForHumanDecision 使用
     */
    private static final Scanner CONSOLE_SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        // ---------- 1. 构建图并打印 Mermaid ----------
        StateGraph<AgentState> graph = getLoopGraph();
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "human-in-loop Graph", true).content());

        // ---------- 2. 配置：在 node-2、node-reset 前中断，并使用 MemorySaver 做 checkpoint ----------
        BaseCheckpointSaver saver = new MemorySaver();
        CompileConfig compileConfig = CompileConfig.builder()
                .interruptBefore("node-2")
                .interruptBefore("node-reset")
                .checkpointSaver(saver)
                .interruptBeforeEdge(false)
                .build();

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("thread-1")
                .build();

        // ---------- 3. 首次执行：跑到第一次中断（interruptBefore）后 stream 结束 ----------
        CompiledGraph<AgentState> workflow = graph.compile(compileConfig);
        AsyncGenerator.Cancellable<NodeOutput<AgentState>> stream = workflow.stream(Map.of(), runnableConfig);
        for (NodeOutput<AgentState> output : stream) {
            System.out.println(output.node() + "->" + output.state().value(LOOP_COUNT_KEY).orElse(0));
        }

        // ---------- 4. 循环：取 checkpoint → 按需更新状态（含 reset 时清零 loopCount）→ resume 直至用户选 Q 或无快照 ----------
        boolean isQuit = false;
        while (true) {
            StateSnapshot<AgentState> snapshot = workflow.getState(runnableConfig);
            if (snapshot == null || isQuit) {
                break;
            }
            System.out.println("snapshot=>" + snapshot.state().data());

            AsyncGenerator.Cancellable<NodeOutput<AgentState>> streamResume;
            if (snapshot.next().contains("reset")) {
                System.out.println("reset");
                streamResume = workflow.stream(GraphInput.resume(), workflow.updateState(runnableConfig, Map.of(LOOP_COUNT_KEY, 0)));
                //reset后，清空历史checkpoint
                saver.release(runnableConfig);
            } else {
                streamResume = workflow.stream(GraphInput.resume(), workflow.updateState(runnableConfig, snapshot.state().data()));
            }
            for (NodeOutput<AgentState> output : streamResume) {
                System.out.println(output.node() + "->" + output.state().value(LOOP_COUNT_KEY).orElse(0));
                if (output.node().contains("END")) {
                    isQuit = true;
                }
            }
            Thread.sleep(20);
        }
        System.out.println("done");
    }

    /**
     * 构建人机协同的循环图：node-1 自增 loopCount，根据控制台输入路由到 node-2 / node-reset / node-1 / END。
     *
     * @return 已配置节点与条件边的 StateGraph
     * @throws GraphStateException 图状态异常
     */
    public static StateGraph<AgentState> getLoopGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(state -> Map.of(LOOP_COUNT_KEY, (int) state.value(LOOP_COUNT_KEY).orElse(0) + 1)))
                .addNode("node-2", node_async(state -> Map.of()))
                .addNode("node-reset", node_async(state -> Map.of()))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-2", "node-1")
                .addEdge("node-reset", "node-1")
                .addConditionalEdges("node-1", state -> CompletableFuture.supplyAsync(HumanInLoopGraph2Application::waitForHumanDecision),
                        Map.of(
                                "exit", GraphDefinition.END,
                                "reset", "node-reset",
                                "next", "node-2",
                                "back", "node-1"));
    }

    /**
     * 在控制台阻塞等待用户输入，映射为图的条件边取值。
     * <ul>
     *   <li>N → "next"（到 node-2）</li>
     *   <li>B → "back"（回到 node-1）</li>
     *   <li>R → "reset"（到 node-reset，外部可配合将 loopCount 置 0）</li>
     *   <li>Q → "exit"（到 END）</li>
     * </ul>
     *
     * @return 条件边键："next" | "back" | "reset" | "exit"
     */
    private static String waitForHumanDecision() {
        while (true) {
            System.out.print("请输入 N(next) 继续到 node-2，B(back) 退回到 node-1，R(Reset) 重置，或 Q(Quit) 结束 [N/B/R/Q]: ");
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
            if ("R".equals(trimmed)) {
                return "reset";
            }
            if ("B".equals(trimmed)) {
                return "back";
            }
            if ("Q".equals(trimmed)) {
                return "exit";
            }
            System.out.println("无效输入，请只输入[N/B/R/Q]");
        }
    }
}
