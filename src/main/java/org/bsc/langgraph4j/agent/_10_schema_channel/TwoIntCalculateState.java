package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.Map;

/**
 * 两步整数运算的图状态。
 * <p>
 * 支持两个运算槽位：op1（第一步）与 op2（第二步）。每步有 num1、num2、operator，
 * 以及 result_1、result_2。第二步的 num1 可由第一步的结果通过 Action 的 forwardResultToKey 写入，
 * 实现“第二步的 num1 = 第一步结果”的串联。
 * </p>
 *
 * @author junmingyang
 */
public class TwoIntCalculateState extends AgentState {

    /** 第一步运算的槽位前缀，对应 op1_num1、op1_num2、op1_operator */
    public static final String OP1_PREFIX = "op1";
    /** 第二步运算的槽位前缀，对应 op2_num1、op2_num2、op2_operator */
    public static final String OP2_PREFIX = "op2";

    /** 状态 Schema：两步的输入与两个结果 channel */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            OP1_PREFIX + "_num1", Channels.base(() -> 0),
            OP1_PREFIX + "_num2", Channels.base(() -> 0),
            OP1_PREFIX + "_operator", Channels.base(() -> "+"),
            OP2_PREFIX + "_num1", Channels.base(() -> 0),
            OP2_PREFIX + "_num2", Channels.base(() -> 0),
            OP2_PREFIX + "_operator", Channels.base(() -> "*"),
            "result_1", Channels.base(() -> 0),
            "result_2", Channels.base(() -> 0)
    );

    /**
     * 使用初始数据构造状态（通常由图的 invoke 传入）。
     *
     * @param initData 初始键值对，如 op1_num1、op1_num2、op1_operator、op2_num2、op2_operator 等
     */
    public TwoIntCalculateState(Map<String, Object> initData) {
        super(initData);
    }

    /**
     * 按槽位前缀取第一个操作数。
     *
     * @param prefix 槽位前缀，如 {@link #OP1_PREFIX} 或 {@link #OP2_PREFIX}
     * @return 对应 prefix_num1 的值，缺省为 0
     */
    public Integer num1(String prefix) {
        return this.<Integer>value(prefix + "_num1").orElse(0);
    }

    /**
     * 按槽位前缀取第二个操作数。
     *
     * @param prefix 槽位前缀
     * @return 对应 prefix_num2 的值，缺省为 0
     */
    public Integer num2(String prefix) {
        return this.<Integer>value(prefix + "_num2").orElse(0);
    }

    /**
     * 按槽位前缀取运算符。
     *
     * @param prefix 槽位前缀
     * @return 对应 prefix_operator 的值，缺省为 "+"
     */
    public String operator(String prefix) {
        return this.<String>value(prefix + "_operator").orElse("+");
    }

    /**
     * 第一步运算的结果。
     *
     * @return result_1 的值，缺省为 0
     */
    public Integer result1() {
        return this.<Integer>value("result_1").orElse(0);
    }

    /**
     * 第二步运算的结果。
     *
     * @return result_2 的值，缺省为 0
     */
    public Integer result2() {
        return this.<Integer>value("result_2").orElse(0);
    }

    /**
     * 最后一步的结果，便于统一取最终值（当前为 result_2）。
     *
     * @return 第二步结果
     */
    public Integer result() {
        return result2();
    }
}
