# Langgraph4j Agent 示例项目

基于 [Langgraph4j](https://github.com/bsorrentino/langgraph4j) 与 Spring Boot 的智能体（Agent）示例工程，涵盖问答 Agent、子图中断与多 Agent 交接等场景。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 21+ |
| Spring Boot | 3.4.5 |
| Langgraph4j | 1.8.0-beta1 |
| Spring AI | 1.1.0 |

## 项目结构

```
src/main/java/org/bsc/langgraph4j/agent/
├── _01_basic/                    # 基础 Q&A Agent（人机协作）
│   ├── langgraph/                # 图定义与状态
│   │   ├── QAAssistant.java      # 问答助手图
│   │   └── QAState.java          # 状态定义
│   ├── controller/               # REST API
│   │   ├── ChatController.java
│   │   ├── ChatRequest.java
│   │   └── ChatResponse.java
│   ├── LG4jQAAgentApplication.java
│   ├── LG4jQAAgentConsole.java   # 控制台交互
│   └── README.md
├── _02_sub_interrupt/            # 子图中断示例
│   ├── AgenticWorkflowWithSubgraphInterruption.java
│   ├── DemoConsoleController.java
│   └── SpringDemoApplication.java
└── _03_springai_agents_handoff/  # Spring AI 多 Agent 交接
    ├── AbstractAgent.java
    ├── AbstractAgentExecutor.java
    ├── AgentHandoff.java
    ├── AgentMarketplace.java
    ├── AgentPayment.java
    └── README.md
```

## 模块说明

### 1. 基础 Q&A Agent（`_01_basic`）

人机协作（Human-in-the-Loop, HITL）示例：通过 Checkpoint 实现中断与恢复，模拟“问国家 → 问城市 → 展示天气”的对话流程。

- **图节点**：`ask_country` → `wait_for_country` → `ask_city` → `wait_for_city` → `show_weather`
- **交互方式**：
  - **REST API**：`POST /chat`，支持 `sessionId` 维持会话
  - **控制台**：运行主类后在终端输入问题与反馈（兼容 IDE 运行，无 `System.console()` 时使用 `Scanner`）

**运行方式**：

```bash
# 控制台模式（默认）
mvn spring-boot:run

# 若需 Web 模式（Studio 等），可修改 application.yml 中 web-application-type
```

**REST 示例**（见 `_01_basic/test.http`）：

- 首次请求不带 `sessionId`，获得 Agent 回复与 `sessionId`
- 后续请求带同一 `sessionId`，按流程输入国家、城市等

---

### 2. 子图中断（`_02_sub_interrupt`）

演示在 Langgraph4j 中如何通过**子图中断**（Subgraph Interruption）在子流程中暂停并向上抛出状态，由上层决定是否恢复。

- 使用 `AsyncCommandAction`、自定义 `SubGraphInterruptionException` 与 Checkpoint 机制
- 适合需要“子图执行到某一步后暂停、由外部驱动再继续”的工作流

独立入口：`SpringDemoApplication`（若需单独运行可配置该主类）。

---

### 3. Spring AI 多 Agent 交接（`_03_springai_agents_handoff`）

基于 Spring AI 的 **多 Agent 交接（Handoff）** 实现：不同 Agent 通过 Tool 调用互相移交任务。

- **AbstractAgent**：抽象 Agent，与 Spring AI `ToolContext`、`ToolCallback` 等集成
- **AbstractAgentExecutor**：执行器，驱动 Agent 调用与 Tool 执行
- **AgentHandoff**：交接语义封装
- **AgentMarketplace** / **AgentPayment**：示例 Agent（如商城、支付），用于集成测试

**运行与测试**：

- 使用 [Javelit](https://docs.javelit.io) 运行示例 App（见该模块内 `README.md`）
- 集成测试：`MultiAgentHandoffITest`（依赖 Spring AI Ollama/OpenAI，部分为 test scope）

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+

### 构建与运行

```bash
# 编译
mvn clean compile

# 运行主应用（_01_basic 控制台模式）
mvn spring-boot:run

# 打包
mvn clean package
```

### 配置说明

- **application.yml**  
  - `spring.main.web-application-type`：`none` 为控制台应用，`servlet` 为 Web 应用（如配合 Studio 或仅用 REST）。

- **依赖说明**  
  - `langgraph4j-core`、`langgraph4j-springai-agentexecutor`：核心与 Spring AI 集成  
  - `spring-ai-ollama` / `spring-ai-openai`：仅在 test scope，用于多 Agent 交接等测试

---

## 文档与参考

- [_01_basic 图与测试场景](src/main/java/org/bsc/langgraph4j/agent/_01_basic/README.md)
- [_03 Spring AI Agents Handoff](src/main/java/org/bsc/langgraph4j/agent/_03_springai_agents_handoff/README.md)
- [Langgraph4j](https://github.com/bsorrentino/langgraph4j)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)

---

## 许可证

与所使用之 Langgraph4j、Spring Boot、Spring AI 等组件的许可证保持一致。
