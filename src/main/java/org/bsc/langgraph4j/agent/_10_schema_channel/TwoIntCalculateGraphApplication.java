package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 演示：SCHEMA与Channel的使用
 * 两步整数运算图应用：固定流程为「先算第一步（op1），再算第二步（op2）」，
 * 第二步的 num1 由第一步的计算结果自动填入，实现步骤间串联。
 * <p>
 * 所有参与计算的值在 {@link #main(String[])} 中通过初始状态一次性传入；
 * 第一步需提供 op1_num1、op1_num2、op1_operator，第二步只需 op2_num2、op2_operator（op2_num1 由第一步结果写入）。
 * </p>
 *
 * @author junmingyang
 */
public class TwoIntCalculateGraphApplication {

    /**
     * 入口：构建图、传入初始状态并执行，打印 Mermaid 图与两步运算结果。
     *
     * @param args 未使用
     * @throws GraphStateException 图构建或状态异常
     */
    public static void main(String[] args) throws GraphStateException {
        StateGraph<TwoIntCalculateState> graph = getSequenceGraph();

        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "Two Int Calculate Graph", true).content());

        Map<String, Object> initState = Map.of(
                TwoIntCalculateState.OP1_PREFIX + "_num1", 3, TwoIntCalculateState.OP1_PREFIX + "_num2", 6, TwoIntCalculateState.OP1_PREFIX + "_operator", "+",
                TwoIntCalculateState.OP2_PREFIX + "_num2", 2, TwoIntCalculateState.OP2_PREFIX + "_operator", "*"
        );

        graph.compile().invoke(initState).ifPresent(c -> {
            int n1 = c.num1(TwoIntCalculateState.OP1_PREFIX);
            int n2 = c.num2(TwoIntCalculateState.OP1_PREFIX);
            String op1 = c.operator(TwoIntCalculateState.OP1_PREFIX);
            int r1 = c.result1();
            int r2 = c.result2();

            int m1 = c.num1(TwoIntCalculateState.OP2_PREFIX);
            int m2 = c.num2(TwoIntCalculateState.OP2_PREFIX);
            String op2 = c.operator(TwoIntCalculateState.OP2_PREFIX);

            System.out.println("步骤1 : " + n1 + op1 + n2 + " = " + r1);
            System.out.println("步骤2 : " + m1 + op2 + m2 + " = " + r2 + " (num1 来自步骤1结果)");
            System.out.println("最终 result = " + r2);
        });
    }

    /**
     * 构建两步运算的序列图：node-1 执行 op1 并结果写入 result_1 与 op2_num1，node-2 执行 op2 并结果写入 result_2。
     *
     * @return 已配置 START -> node-1 -> node-2 -> END 的 StateGraph
     * @throws GraphStateException 图构建异常
     */
    public static StateGraph<TwoIntCalculateState> getSequenceGraph() throws GraphStateException {
        return new StateGraph<>(TwoIntCalculateState.SCHEMA, TwoIntCalculateState::new)
                .addNode("node-1", node_async(new TwoIntCalculateAction(TwoIntCalculateState.OP1_PREFIX, "result_1", TwoIntCalculateState.OP2_PREFIX + "_num1")))
                .addNode("node-2", node_async(new TwoIntCalculateAction(TwoIntCalculateState.OP2_PREFIX, "result_2")))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-2", GraphDefinition.END);
    }
}
