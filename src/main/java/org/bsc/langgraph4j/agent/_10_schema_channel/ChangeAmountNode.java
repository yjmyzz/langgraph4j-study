package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.action.NodeAction;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author junmingyang
 */
public class ChangeAmountNode implements NodeAction<OrderState> {

    @Override
    public Map<String, Object> apply(OrderState state) throws Exception {
        System.out.println("current node: changeAmount , data:" + state);
        int newAmount = state.amount() + 1;
        BigDecimal newTotal = state.price().multiply(BigDecimal.valueOf(newAmount));
        return Map.of(OrderState.ORDER_REMARK, "数量+1",
                OrderState.ORDER_AMOUNT, newAmount,
                OrderState.ORDER_TOTAL, newTotal);
    }
}
