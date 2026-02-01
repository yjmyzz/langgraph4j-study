package org.bsc.langgraph4j.agent._03_springai_agents_handoff;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentPayment extends AbstractAgentExecutor<AgentPayment.Builder> {

    static class Tools {

        record Transaction(
                @JsonPropertyDescription("购买的产品名称") String product,
                @JsonPropertyDescription("操作代码") String code
        ) {}

        @Tool(description="为购买特定产品提交支付")
        Transaction submitPayment(
                @ToolParam( description="要购买的产品名称") String product,
                @ToolParam( description="产品价格") double price,
                @ToolParam( description="产品价格货币单位") String currency,
                @ToolParam( description="国际银行账号 (IBAN)") String iban,
                ToolContext toolContext ) {
            return new Transaction( product,"123456789A" );

        }

        @Tool(description="获取IBAN信息")
        String retrieveIBAN()  {
            return """
                    GB82WEST12345698765432
                    """;
        }

    }

    public static class Builder extends AbstractAgentExecutor.Builder<Builder> {

        public AgentPayment build() throws GraphStateException {
            this.name("payment")
                    .description("支付智能体，处理购买和支付交易请求")
                    .parameterDescription("所有购买信息以便完成支付")
                    .defaultSystem( """
                你是提供支付服务的智能体。
                """)
                    .toolsFromObject( new Tools() )
            ;

            return new AgentPayment( this );
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    protected AgentPayment(Builder builder) throws GraphStateException {
        super(builder);
    }
}