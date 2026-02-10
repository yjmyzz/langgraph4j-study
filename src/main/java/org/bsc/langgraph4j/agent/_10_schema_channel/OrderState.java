package org.bsc.langgraph4j.agent._10_schema_channel;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Reducer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.SimpleFormatter;

/**
 * @author junmingyang
 */
public class OrderState extends AgentState {

    static Date DEFAULT_DATE = Date.from(LocalDate.of(1999, 1, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public static final String ORDER_ID = "orderId";
    public static final String ORDER_AMOUNT = "amount";
    public static final String ORDER_PRICE = "price";
    public static final String ORDER_TOTAL = "total";
    public static final String ORDER_DATE = "orderDate";
    public static final String ORDER_REMARK = "remark";


    //订单状态的Schema
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            ORDER_ID, Channels.base(() -> ""), //订单号，默认值
            ORDER_AMOUNT, Channels.base(() -> 0), //订单数量，默认值
            ORDER_PRICE, Channels.base(OrderState::min), //订单价格，使用自定义策略更新，始终取最低价
            ORDER_TOTAL, Channels.base(() -> BigDecimal.valueOf(0)), //订单总价，默认值0
            ORDER_DATE, Channels.base(() -> DEFAULT_DATE), //订单日期，默认值
            ORDER_REMARK, Channels.appender(ArrayList::new) //订单备注，使用“追加”策略更新
    );

    /**
     * 这里模拟始终取最低价格
     *
     * @param a
     * @param b
     * @return
     */
    static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.min(b);
    }

    public OrderState(Map<String, Object> initData) {
        super(initData);
    }

    public String orderId() {
        return this.<String>value(ORDER_ID).orElse("");
    }

    public Integer amount() {
        return this.<Integer>value(ORDER_AMOUNT).orElse(0);
    }

    public BigDecimal price() {
        return this.<BigDecimal>value(ORDER_PRICE).orElse(BigDecimal.valueOf(0));
    }

    public BigDecimal total() {
        return this.<BigDecimal>value(ORDER_TOTAL).orElse(BigDecimal.valueOf(0));
    }

    public Date orderDate() {
        return this.<Date>value(ORDER_DATE).orElse(DEFAULT_DATE);
    }

    public List<String> remark() {
        return this.<List<String>>value(ORDER_REMARK).orElse(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "OrderState{" +
                "orderId='" + orderId() + '\'' +
                ", amount=" + amount() +
                ", price=" + price() +
                ", total=" + total() +
                ", orderDate=" + SDF.format(orderDate()) +
                ", remark=" + remark() +
                '}';
    }

}
