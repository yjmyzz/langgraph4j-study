package org.bsc.langgraph4j.agent._03_springai_agents_handoff;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentMarketplace extends AbstractAgentExecutor<AgentMarketplace.Builder> {

    static class Tools {
        record Product(
                @JsonPropertyDescription("产品名称") String name,
                @JsonPropertyDescription("产品价格") double price,
                @JsonPropertyDescription("产品价格货币单位") String currency) {}

        @Tool( description="在市场中搜索特定产品")
        Product searchByProduct(@ToolParam( description="要搜索的产品名称") String product, ToolContext toolContext) {
            return new Product( "X", 1000, "EUR" );
        }

    }

    public static class Builder extends AbstractAgentExecutor.Builder<Builder> {

        public AgentMarketplace build() throws GraphStateException {
            this.name("marketplace")
                    .description("市场AI助手，提供产品相关信息查询")
                    .parameterDescription("关于产品的所有信息请求")
                    .defaultSystem( """
                    你是提供产品市场信息的智能体。
                """)
                    .toolsFromObject( new Tools() )
            ;

            return new AgentMarketplace( this );
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    protected AgentMarketplace(Builder builder) throws GraphStateException {
        super(builder);
    }

}