package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.action.NodeAction;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author junmingyang
 */
public class ChangePriceNode implements NodeAction<OrderState> {

    @Override
    public Map<String, Object> apply(OrderState state) throws Exception {
        System.out.println("current node: changePrice , data:" + state);
        return Map.of(OrderState.ORDER_REMARK, "上涨价格10%",
                OrderState.ORDER_PRICE, state.price().multiply(BigDecimal.valueOf(1.1)));
    }
}
