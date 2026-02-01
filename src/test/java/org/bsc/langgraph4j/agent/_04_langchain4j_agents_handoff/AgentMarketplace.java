package org.bsc.langgraph4j.agent._04_langchain4j_agents_handoff;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.output.structured.Description;

public class AgentMarketplace extends AbstractAgentService<AgentMarketplace.Builder> {

    static class Tools {
        record Product(
                @Description("产品名称") String name,
                @Description("产品价格") double price,
                @Description("产品价格货币单位") String currency) {
        }

        @Tool("搜索特定产品")
        Product searchByProduct(@P("要搜索的产品名称") String productName) {
            System.out.printf("[工具调用] searchByProduct=>%s %d %s%n%n", productName, 1399, "元");
            return new Product(productName, 1399D, "元");
        }

    }

    public static class Builder extends AbstractAgentService.Builder<Builder> {

        public AgentMarketplace build() {
            this.name("marketplace")
                    .description("市场AI助手，提供产品相关信息查询")
                    .singleParameter("关于产品的所有信息请求")
                    .systemMessage(SystemMessage.from("""
                                你是提供产品信息的AI助手。
                            """))
                    .toolFromObject(new Tools());
            return new AgentMarketplace(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }


    public AgentMarketplace(Builder builder) {
        super(builder);
    }

}