# MultiAgentHandoffITest 调用时序图

## 测试方法: testHandoff()

```mermaid
sequenceDiagram
    autonumber
    participant Test as MultiAgentHandoffITest
    participant AiModel as AiModel
    participant Marketplace as AgentMarketplace
    participant Payment as AgentPayment
    participant Handoff as AgentHandoff
    participant CompiledGraph as CompiledGraph(Handoff)
    participant ChatModel as ChatModel(minimax-m2)
    participant MarketplaceExecutor as AgentMarketplace.agentExecutor
    participant PaymentExecutor as AgentPayment.agentExecutor

    Note over Test: 1. 构建阶段
    Test->>AiModel: OLLAMA.chtModel(modelName)
    AiModel-->>Test: ChatModel

    Test->>Marketplace: builder().chatModel(model).build()
    Marketplace->>Marketplace: 初始化 Tools(searchByProduct)
    Marketplace-->>Test: AgentMarketplace 实例

    Test->>Payment: builder().chatModel(model).build()
    Payment->>Payment: 初始化 Tools(submitPayment, retrieveIBAN)
    Payment-->>Test: AgentPayment 实例

    Test->>Handoff: builder().chatModel(model).agent(marketplace).agent(payment).build()
    Handoff->>Handoff: delegate.tool(marketplace.asTool())
    Handoff->>Handoff: delegate.tool(payment.asTool())
    Handoff->>Handoff: delegate.build()
    Handoff-->>Test: StateGraph

    Test->>CompiledGraph: .compile()
    CompiledGraph-->>Test: handoffExecutor (CompiledGraph)

    Note over Test: 2. 调用阶段
    Test->>CompiledGraph: invoke(Map.of("messages", UserMessage(input)))
    Note right of Test: input = "search for product 'X' and purchase it with IBAN US82WEST1234567890123456"

    CompiledGraph->>ChatModel: 分析用户消息，决定调用哪个 Agent 工具
    ChatModel-->>CompiledGraph: 工具调用请求 (如: marketplace)

    CompiledGraph->>Marketplace: apply(Request, ToolContext) [作为 Tool 被调用]
    Marketplace->>MarketplaceExecutor: invoke(Map.of("messages", UserMessage(request.input)))
    MarketplaceExecutor->>ChatModel: ReAct 循环: 调用 searchByProduct("X")
    ChatModel-->>MarketplaceExecutor: 工具结果
    MarketplaceExecutor-->>Marketplace: 最终 AssistantMessage
    Marketplace-->>CompiledGraph: 产品信息 (Product X, 1000 EUR)

    CompiledGraph->>ChatModel: 继续处理，决定下一步 (如: 调用 payment)
    ChatModel-->>CompiledGraph: 工具调用请求 (payment)

    CompiledGraph->>Payment: apply(Request, ToolContext) [作为 Tool 被调用]
    Payment->>PaymentExecutor: invoke(Map.of("messages", UserMessage(...)))
    PaymentExecutor->>ChatModel: ReAct 循环: 调用 submitPayment(product, price, currency, iban)
    ChatModel-->>PaymentExecutor: 工具结果
    PaymentExecutor-->>Payment: 最终 AssistantMessage
    Payment-->>CompiledGraph: 交易确认 (Transaction 123456789A)

    CompiledGraph->>ChatModel: 汇总结果，生成最终响应
    ChatModel-->>CompiledGraph: 最终 AssistantMessage
    CompiledGraph-->>Test: Optional<State> (含 messages)

    Note over Test: 3. 结果提取
    Test->>Test: result.flatMap(MessagesState::lastMessage)
    Test->>Test: .map(Content::getText).orElseThrow()
    Test->>Test: System.out.println(response)
```

## 流程图：testHandoff() 执行流程

```mermaid
flowchart TB
    subgraph 构建阶段["1. 构建阶段"]
        A([开始]) --> B[AiModel.OLLAMA.chtModel]
        B --> C[AgentMarketplace.builder<br/>.chatModel.build]
        C --> D[AgentPayment.builder<br/>.chatModel.build]
        D --> E[AgentHandoff.builder<br/>.chatModel.agent.agent.build]
        E --> F[.compile]
        F --> G[得到 handoffExecutor]
    end

    subgraph 调用阶段["2. 调用阶段"]
        G --> H[handoffExecutor.invoke<br/>UserMessage 输入]
        H --> I{Handoff ChatModel<br/>分析用户意图}
        I -->|需查产品| J[调用 marketplace 工具]
        I -->|需支付| K[调用 payment 工具]
        I -->|已完成| L[生成最终回复]

        J --> J1[AgentMarketplace.apply]
        J1 --> J2[内部 ReAct: searchByProduct]
        J2 --> I

        K --> K1[AgentPayment.apply]
        K1 --> K2[内部 ReAct: submitPayment]
        K2 --> I
    end

    subgraph 结果阶段["3. 结果阶段"]
        L --> M[result.flatMap lastMessage]
        M --> N[.map getText]
        N --> O([输出 response])
    end
```

## 简化版时序图（仅核心调用链）

```mermaid
sequenceDiagram
    participant Test as MultiAgentHandoffITest
    participant Handoff as AgentHandoff (CompiledGraph)
    participant ChatModel as ChatModel
    participant Marketplace as AgentMarketplace
    participant Payment as AgentPayment

    Test->>Handoff: invoke(messages)
    Handoff->>ChatModel: 路由与编排
    ChatModel->>Marketplace: 调用 marketplace 工具 (apply)
    Marketplace-->>Handoff: 产品信息
    Handoff->>ChatModel: 继续编排
    ChatModel->>Payment: 调用 payment 工具 (apply)
    Payment-->>Handoff: 支付结果
    Handoff->>ChatModel: 生成最终回复
    ChatModel-->>Handoff: 最终消息
    Handoff-->>Test: result.flatMap(lastMessage).map(getText)
```

## 关键类说明

| 参与者 | 说明 |
|--------|------|
| **MultiAgentHandoffITest** | 测试类，构建 AgentMarketplace、AgentPayment、AgentHandoff 并调用 invoke |
| **AgentHandoff** | 协调器，将 Marketplace 与 Payment 注册为工具，内部使用 AgentExecutor/StateGraph |
| **AgentMarketplace** | 市场代理，暴露 `searchByProduct` 工具，作为 Handoff 的一个子工具被调用 |
| **AgentPayment** | 支付代理，暴露 `submitPayment`、`retrieveIBAN` 工具，作为 Handoff 的一个子工具被调用 |
| **AbstractAgentExecutor.apply()** | 当 Handoff 的 LLM 决定调用某 Agent 时，会执行对应 Agent 的 apply，内部再 invoke 该 Agent 的 CompiledGraph |

## 数据流摘要

1. **构建**: Test → AiModel 取 ChatModel → 创建 Marketplace、Payment（各自持有 ChatModel 与 Tools）→ 创建 Handoff 并将两者注册为 tool → compile 得到 handoffExecutor。
2. **执行**: Test 调用 handoffExecutor.invoke(messages) → Handoff 的 ChatModel 解析用户意图 → 按需调用 marketplace 或 payment 的 apply() → 各 Agent 内部执行自己的 ReAct（调用 searchByProduct / submitPayment 等）→ 结果回填到 Handoff → Handoff 的 ChatModel 生成最终回复。
3. **取结果**: Test 从返回的 State 中 lastMessage，再 getText，得到最终响应文本。
