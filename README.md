# Langgraph4j Agent 示例项目

基于 [Langgraph4j](https://github.com/bsorrentino/langgraph4j) 与 Spring Boot 的智能体（Agent）示例工程，涵盖顺序图、条件分支、并行、循环、人机循环（HITL）、子图中断、Schema Channel 两步运算、Spring AI 多 Agent 交接与 Langchain4j 多 Agent 交接等场景。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 21+ |
| Spring Boot | 3.4.5 |
| Langgraph4j | 1.8.0-beta1 |
| Spring AI | 1.1.0 |
| Langchain4j | 1.10.0 |

## 项目结构

```
src/main/java/org/bsc/langgraph4j/agent/
├── _01_basic/                    # 基础 Q&A Agent（人机协作 HITL）
│   ├── langgraph/
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
├── _03_springai_agents_handoff/  # Spring AI 多 Agent 交接
│   ├── AbstractAgent.java
│   ├── AbstractAgentExecutor.java
│   ├── AgentHandoff.java
│   └── README.md
├── _04_langchain4j_agents_handoff/  # Langchain4j 多 Agent 交接
│   ├── AbstractAgent.java
│   ├── AbstractAgentExecutor.java
│   ├── AbstractAgentService.java
│   └── AgentHandoff.java
├── _05_sequence/                # 顺序图
│   ├── SequenceGraphApplication.java
│   ├── Node1Action.java
│   └── Node2Action.java
├── _06_conditional/              # 条件分支图
│   ├── ConditionalGraphApplication.java
│   ├── Node1Action.java
│   ├── Node2Action.java
│   ├── Node3Action.java
│   └── RoutingEdgeAction.java
├── _07_parallel/                 # 并行图
│   ├── ParallelGraphApplication.java
│   ├── Node1Action.java
│   ├── Node2Action.java
│   └── Node3Action.java
├── _08_loop/                     # 循环图（固定次数退出）
│   ├── LoopGraphApplication.java
│   ├── Node1Action.java
│   └── Node2Action.java
├── _09_human_in_loop/            # 人机循环（控制台 C/Q 决定继续或结束）
│   ├── HumanInLoopGraphApplication.java
│   ├── Node1Action.java
│   └── Node2Action.java
└── _10_schema_channel/           # Schema Channel：两步运算、步骤间结果串联
    ├── TwoIntCalculateGraphApplication.java
    ├── TwoIntCalculateState.java
    └── TwoIntCalculateAction.java

src/test/java/org/bsc/langgraph4j/agent/
├── _03_springai_agents_handoff/  # Spring AI 交接示例与测试
│   ├── AgentMarketplace.java     # 市场 Agent（searchByProduct）
│   ├── AgentPayment.java         # 支付 Agent（submitPayment, retrieveIBAN）
│   ├── AiModel.java              # Ollama/OpenAI 模型封装
│   ├── JtMultiAgentHandoffApp.java   # Javelit 示例 App
│   ├── MultiAgentHandoffITest.java   # 集成测试
│   ├── MultiAgentHandoffITest_sequence_diagram.md   # 时序图与流程图
│   └── *.png                     # 导出的序列图/流程图
├── _04_langchain4j_agents_handoff/
│   ├── AgentMarketplace.java
│   ├── AgentPayment.java
│   └── MultiAgentHandoffITest.java
└── studio/                       # LangGraph Studio 示例配置
    ├── LangGraphStudioApplication.java
    └── LangGraphStudioSampleConfig.java
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

# Studio Web 模式
mvn spring-boot:run -Dspring.profiles.active=studio
```

**REST 示例**（见 `_01_basic/test.http`）：

- 首次请求不带 `sessionId`，获得 Agent 回复与 `sessionId`
- 后续请求带同一 `sessionId`，按流程输入国家、城市等

**Studio 模式**：

- 启动命令：`mvn spring-boot:run -Dspring.profiles.active=studio`
- 访问地址：`http://localhost:8080`
- 功能：提供图形化界面来交互和调试 LangGraph 智能体

---

### 2. 子图中断（`_02_sub_interrupt`）

演示在 Langgraph4j 中如何通过**子图中断**（Subgraph Interruption）在子流程中暂停并向上抛出状态，由上层决定是否恢复。

- 使用 `AsyncCommandAction`、自定义 `SubGraphInterruptionException` 与 Checkpoint 机制
- 适合需要“子图执行到某一步后暂停、由外部驱动再继续”的工作流

独立入口：`SpringDemoApplication`（若需单独运行可配置该主类）。

---

### 3. Spring AI 多 Agent 交接（`_03_springai_agents_handoff`）

基于 Spring AI 的 **多 Agent 交接（Handoff）** 实现：Handoff 将多个子 Agent 注册为工具，由主 Agent 的 ChatModel 根据用户意图决定调用哪个 Agent（如先查产品再支付）。

- **main**：`AbstractAgent`（与 Spring AI `ToolContext`、`ToolCallback` 集成）、`AbstractAgentExecutor`（内部 ReAct + CompiledGraph）、`AgentHandoff`（交接编排）
- **test**：`AgentMarketplace`（工具 `searchByProduct`）、`AgentPayment`（工具 `submitPayment`、`retrieveIBAN`）、`AiModel`、`JtMultiAgentHandoffApp`、`MultiAgentHandoffITest`

**运行与测试**：

- 使用 [Javelit](https://docs.javelit.io) 运行示例 App：见该模块内 [README](src/main/java/org/bsc/langgraph4j/agent/_03_springai_agents_handoff/README.md)
- 集成测试：运行 `MultiAgentHandoffITest#testHandoff`（需配置 Ollama 或 OpenAI，依赖在 test scope）
- **时序图与流程图**：[MultiAgentHandoffITest_sequence_diagram.md](src/test/java/org/bsc/langgraph4j/agent/_03_springai_agents_handoff/MultiAgentHandoffITest_sequence_diagram.md)（含 Mermaid 时序图与流程图）

---

### 4. Langchain4j 多 Agent 交接（`_04_langchain4j_agents_handoff`）

基于 **Langchain4j** 的多 Agent 交接实现，与 _03 语义类似，底层使用 Langchain4j 的 Tool/Agent 能力。

- **main**：`AbstractAgent`、`AbstractAgentExecutor`、`AbstractAgentService`、`AgentHandoff`
- **test**：`AgentMarketplace`、`AgentPayment`、`MultiAgentHandoffITest`

---

### 5. 顺序图（`_05_sequence`）

演示 Langgraph4j 最基础的**顺序执行**图：`START → node-1 → node-2 → END`。用于理解节点与边的构建、Mermaid 图导出以及图的执行与状态输出。

- **入口**：`SequenceGraphApplication#main`

---

### 6. 条件分支图（`_06_conditional`）

演示**条件边（Conditional Edges）**：从 node-1 根据状态（如 `routeTo`）动态路由到 node-2 或 node-3，再汇聚到 END。支持默认路由与 Mermaid 可视化。

- **入口**：`ConditionalGraphApplication#main`
- **路由逻辑**：`RoutingEdgeAction` 根据 state 返回目标节点标识

---

### 7. 并行图（`_07_parallel`）

演示**并行分支**：node-1 执行完成后，node-2 与 node-3 并行执行，再分别到 END。用于理解多分支并行执行。

- **入口**：`ParallelGraphApplication#main`
- **图结构**：`START → node-1 → [node-2, node-3] → END`

---

### 8. 循环图（`_08_loop`）

演示**固定次数循环**：`START → node-1 → (条件) → node-2 → node-1 → … → END`。通过状态中的 `loopCount` 与 `MAX_LOOP_ITERATIONS=3` 控制，循环 3 次后自动退出到 END。

- **入口**：`LoopGraphApplication#main`

---

### 9. 人机循环（`_09_human_in_loop`）

演示**人机协作循环**：流程与 _08_loop 类似，但在 node-1 执行完后由**控制台人工输入**决定下一步：

- **C (Continue)**：进入 node-2，再回到 node-1，继续循环
- **Q (Quit)**：直接到 END，结束图

需在带控制台的环境运行（如 IDE 或终端），使用共享 `Scanner` 读取 `System.in`，避免关闭标准输入导致后续 `No line found`。

- **入口**：`HumanInLoopGraphApplication#main`

---

### 10. Schema Channel 两步运算（`_10_schema_channel`）

演示基于 **Schema / Channel** 的状态设计：固定流程为两步运算（先 op1，再 op2），**第二步的 num1 由第一步的计算结果自动填入**，实现节点间结果串联。

- **状态**：`TwoIntCalculateState` 定义两槽位（op1、op2）的 num1/num2/operator 及 result_1、result_2；支持在 invoke 前一次性传入所有参数（第一步完整入参，第二步仅需 num2 与 operator）。
- **节点**：`TwoIntCalculateAction` 通用四则运算，通过 `inputPrefix`、`resultKey` 绑定槽位；可选 `forwardResultToKey` 将本步结果写入下一节点输入（如将 result_1 写入 op2_num1）。
- **入口**：`TwoIntCalculateGraphApplication#main`（示例：第一步 3*6，第二步结果/2，即 18/2=9）。

---

## 修复记录

### 最近修复

- **AgentPayment 修复**：修复了 `submitPayment` 方法中格式化字符串错误，将 `%d`（整数格式）改为 `%.2f`（浮点数格式）以正确处理 `double` 类型的价格参数，并确保使用传入的参数而非硬编码值。

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

**独立图示例（_05～_10）**：在 IDE 中直接运行对应主类的 `main` 方法即可，例如：

- 顺序图：`SequenceGraphApplication`
- 条件图：`ConditionalGraphApplication`
- 并行图：`ParallelGraphApplication`
- 循环图：`LoopGraphApplication`
- 人机循环：`HumanInLoopGraphApplication`（需在带控制台环境运行，输入 C/Q）
- Schema Channel 两步运算：`TwoIntCalculateGraphApplication`

### 配置说明

- **application.yml**  
  - `spring.main.web-application-type`：`none` 为控制台应用，`servlet` 为 Web 应用（如配合 Studio 或仅用 REST）。

- **application-studio.yml**  
  - 为LangGraph Studio提供的专用配置，启用Web功能

- **依赖说明**  
  - `langgraph4j-core`、`langgraph4j-springai-agentexecutor`、`langgraph4j-agent-executor`：核心与 Spring AI 集成  
  - `langgraph4j-studio-springboot`：LangGraph Studio Web界面支持  
  - `langchain4j`、`langchain4j-mcp`：Langchain4j 多 Agent 交接（_04）  
  - `spring-ai-ollama` / `spring-ai-openai`：仅在 test scope，用于 _03 多 Agent 交接测试  
  - `javelit`：仅在 test scope，用于 _03 的 Javelit 示例 App

---

## 联系方式
如有问题，请通过以下方式联系：

- 提交GitHub Issue
- 作者博客: http://yjmyzz.cnblogs.com
- 作者: 菩提树下的杨过

## 文档与参考

- [_01_basic 图与测试场景](src/main/java/org/bsc/langgraph4j/agent/_01_basic/README.md)
- [_03 Spring AI Agents Handoff](src/main/java/org/bsc/langgraph4j/agent/_03_springai_agents_handoff/README.md)
- [_03 多 Agent 交接时序图与流程图](src/test/java/org/bsc/langgraph4j/agent/_03_springai_agents_handoff/MultiAgentHandoffITest_sequence_diagram.md)
- [Langgraph4j](https://github.com/bsorrentino/langgraph4j)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)
- [Langchain4j](https://github.com/langchain4j/langchain4j)

---

## 许可证

与所使用之 Langgraph4j、Spring Boot、Spring AI 等组件的许可证保持一致。
