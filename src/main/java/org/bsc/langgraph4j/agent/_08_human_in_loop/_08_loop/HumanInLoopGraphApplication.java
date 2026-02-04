package org.bsc.langgraph4j.agent._08_human_in_loop._08_loop;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @author junmingyang
 */

/**
 * 演示人机交互循环的LangGraph4j应用程序
 * <p>
 * 该应用构建了一个包含人类决策节点的循环图结构：
 * - node-1: 执行主要业务逻辑
 * - node-2: 执行辅助处理逻辑
 * - 人类决策点: 用户可选择继续循环(C)或退出(Q)
 * <p>
 * 程序流程：
 * START -> node-1 -> [人工决策] -> node-2 -> node-1 -> [人工决策] -> END
 *
 * @author 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
public class HumanInLoopGraphApplication {

    /**
     * 共享 Scanner，不关闭以免关闭 System.in导致后续无法读取
     */
    private static final Scanner CONSOLE_SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> sequenceGraph = getLoopGraph();

        System.out.println(sequenceGraph.getGraph(GraphRepresentation.Type.MERMAID, "human-in-loop Graph", false).content());

        sequenceGraph.compile().invoke(Map.of()).ifPresent(c -> {
            System.out.println(c.data());
        });
    }

    public static StateGraph<AgentState> getLoopGraph() throws GraphStateException {
        return new StateGraph<>(AgentState::new)
                .addNode("node-1", node_async(new Node1Action()))
                .addNode("node-2", node_async(new Node2Action()))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-2", "node-1")
                .addConditionalEdges("node-1", state -> CompletableFuture.supplyAsync(HumanInLoopGraphApplication::waitForHumanDecision),
                        Map.of(
                        "exit", GraphDefinition.END,
                        "continue", "node-2"));
    }

    /**
     * 控制台等待用户输入：C(Continue) 进入 node-2，Q(Quit) 结束到 END。
     * 使用共享 CONSOLE_SCANNER，不能关闭否则会关闭 System.in 导致下次 No line found。
     */
    private static String waitForHumanDecision() {
        while (true) {
            System.out.print("请输入 C(Continue) 继续到 node-2，或 Q(Quit) 结束 [C/Q]: ");
            if (!CONSOLE_SCANNER.hasNextLine()) {
                return "exit";
            }
            String input = CONSOLE_SCANNER.nextLine();
            if (input == null) {
                continue;
            }
            String trimmed = input.trim().toUpperCase();
            if ("C".equals(trimmed)) {
                return "continue";
            }
            if ("Q".equals(trimmed)) {
                return "exit";
            }
            System.out.println("无效输入，请只输入 C 或 Q。");
        }
    }
}
