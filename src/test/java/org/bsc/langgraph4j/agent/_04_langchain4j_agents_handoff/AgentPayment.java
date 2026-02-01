package org.bsc.langgraph4j.agent._04_langchain4j_agents_handoff;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.output.structured.Description;

public class AgentPayment extends AbstractAgentService<AgentPayment.Builder> {

    static class Tools {

        record Transaction(
                @Description("购买的产品名称") String product,
                @Description("操作代码") String code
        ) {
        }

        @Tool("提交支付信息")
        Transaction submitPayment(
                @P("要购买的产品名称") String product,
                @P("产品价格") double price,
                @P("产品价格货币单位") String currency,
                @P(value = "国际银行账号 (IBAN)") String iban) {
            System.out.printf("[工具调用] submitPayment=>%s %.2f %s, IBAN:%s %n%n", product, price, currency, iban);
            return new Transaction(product, "123456789A");

        }

        @Tool("获取IBAN信息")
        String retrieveIBAN() {
            System.out.printf("[工具调用] retrieveIBAN=>IBAN:%s %n%n", "GB82WEST12345698765432");
            return """
                    GB82WEST12345698765432
                    """;
        }

    }

    public static class Builder extends AbstractAgentService.Builder<Builder> {

        public AgentPayment build() {
            return new AgentPayment(this.name("payment")
                    .description("支付AI助手，处理购买和支付交易请求")
                    .singleParameter("相关购买信息以便完成支付")
                    .systemMessage(SystemMessage.from("""
                            你是提供支付服务的AI助手。
                            """))
                    .toolFromObject(new Tools()));
        }

    }

    public static Builder builder() {
        return new Builder();
    }


    public AgentPayment(Builder builder) {
        super(builder);
    }

}