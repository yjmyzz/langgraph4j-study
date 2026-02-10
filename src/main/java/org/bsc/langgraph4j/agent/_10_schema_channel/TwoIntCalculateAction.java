package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

/**
 * 两数四则运算节点动作。
 * <p>
 * 根据状态中指定槽位（由 inputPrefix 决定）的 num1、num2、operator 执行加减乘除，
 * 将结果写入 resultKey；可选地将同一结果再写入 forwardResultToKey，用于串联下一节点（如第二步的 num1 取第一步结果）。
 * </p>
 *
 * @author junmingyang
 */
public class TwoIntCalculateAction implements NodeAction<TwoIntCalculateState> {

    private final String inputPrefix;
    private final String resultKey;
    /**
     * 若非空，将本节点计算结果同时写入该 key，供下一节点使用（如 op2_num1 = result_1）
     */
    private final String forwardResultToKey;

    /**
     * 仅写入结果，不转发到下一节点。
     *
     * @param inputPrefix 读取的槽位前缀，如 "op1" 表示使用 op1_num1, op1_num2, op1_operator
     * @param resultKey   写入结果的 channel key，如 "result_1"
     */
    public TwoIntCalculateAction(String inputPrefix, String resultKey) {
        this(inputPrefix, resultKey, null);
    }

    /**
     * 写入结果，并可选择将结果转发到指定 key，供下一节点读取（如将 result_1 写入 op2_num1）。
     *
     * @param inputPrefix        读取的槽位前缀
     * @param resultKey          写入结果的 channel key
     * @param forwardResultToKey 若非空，将计算结果同时写入该 key，用于串联下一节点的 num1
     */
    public TwoIntCalculateAction(String inputPrefix, String resultKey, String forwardResultToKey) {
        this.inputPrefix = inputPrefix;
        this.resultKey = resultKey;
        this.forwardResultToKey = forwardResultToKey;
    }

    /**
     * 从状态中按槽位读取 num1、num2、operator，执行四则运算并写回结果；若配置了 forwardResultToKey 则同时写回该 key。
     *
     * @param state 当前图状态
     * @return 包含 resultKey（及可选的 forwardResultToKey）的更新 Map
     */
    @Override
    public Map<String, Object> apply(TwoIntCalculateState state) throws Exception {
        String operator = state.operator(inputPrefix);
        int num1 = state.num1(inputPrefix);
        int num2 = state.num2(inputPrefix);
        List<String> allowOperators = List.of("+", "-", "*", "/");
        if (allowOperators.contains(operator)) {
            int result = switch (operator) {
                case "+" -> num1 + num2;
                case "-" -> num1 - num2;
                case "*" -> num1 * num2;
                case "/" -> num2 == 0 ? 0 : num1 / num2;
                default -> throw new IllegalArgumentException("Invalid operator: " + operator);
            };
            if (forwardResultToKey != null && !forwardResultToKey.isEmpty()) {
                return Map.of(resultKey, result,
                        forwardResultToKey, result);
            }
            return Map.of(resultKey, result);
        }
        return Map.of(resultKey, 0);
    }
}
