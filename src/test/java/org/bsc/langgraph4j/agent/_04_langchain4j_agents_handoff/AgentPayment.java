package org.bsc.langgraph4j.agent._04_langchain4j_agents_handoff;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.output.structured.Description;
import org.bsc.langgraph4j.GraphStateException;

public class AgentPayment extends AbstractAgentExecutor<AgentPayment.Builder> {

    static class Tools {

        record Transaction(
                @Description("购买的产品名称") String product,
                @Description("操作代码") String code
        ) {}

        @Tool("为购买特定产品提交支付")
        Transaction submitPayment(
                @P("要购买的产品名称") String product,
                @P("产品价格") double price,
                @P("产品价格货币单位") String currency,
                @P(value = "国际银行账号 (IBAN)") String iban ) {
            return new Transaction( product,"123456789A" );

        }

        @Tool("获取IBAN信息")
        String retrieveIBAN()  {
            return """
                    GB82WEST12345698765432
                    """;
        }

    }

    public static class Builder extends AbstractAgentExecutor.Builder<Builder> {

        public AgentPayment build() throws GraphStateException {
            return new AgentPayment( this.name("payment")
                    .description("支付AI助手，处理购买和支付交易请求")
                    .singleParameter("所有购买信息以便完成支付")
                    .systemMessage( SystemMessage.from("""
                    你是提供支付服务的AI助手。
                    """) )
                    .toolFromObject( new Tools() ));
        }

    }

    public static Builder builder() {
        return new Builder();
    }


    public AgentPayment( Builder builder ) throws GraphStateException {
        super(builder);
    }

}