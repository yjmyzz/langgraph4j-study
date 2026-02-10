package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * @author junmingyang
 */
public class SampleOrderGraphApplication {


    public static void main(String[] args) throws GraphStateException {
        StateGraph<OrderState> graph = getSequenceGraph();

        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "Order Graph", true).content());

        Map<String, Object> initState = Map.of(
                OrderState.ORDER_ID, "123456",
                OrderState.ORDER_AMOUNT, 10,
                OrderState.ORDER_PRICE, BigDecimal.valueOf(100),
                OrderState.ORDER_DATE, new Date()
        );

        graph.compile().invoke(initState).ifPresent(c -> {
            System.out.println("订单最终结果：" + c);
        });
    }

    public static StateGraph<OrderState> getSequenceGraph() throws GraphStateException {
        return new StateGraph<>(OrderState.SCHEMA, OrderState::new) //使用Schema构建StateGraph
                .addNode("change-price", node_async(new ChangePriceNode()))
                .addNode("change-amount", node_async(new ChangeAmountNode()))
                .addEdge(GraphDefinition.START, "change-price")
                .addEdge("change-price", "change-amount")
                .addEdge("change-amount", GraphDefinition.END);
    }
}
