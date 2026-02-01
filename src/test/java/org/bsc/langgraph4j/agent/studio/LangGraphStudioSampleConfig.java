package org.bsc.langgraph4j.agent.studio;

import lombok.SneakyThrows;
import org.bsc.langgraph4j.agent._01_basic.langgraph.QAAssistant;
import org.bsc.langgraph4j.agent._02_sub_interrupt.AgenticWorkflowWithSubgraphInterruption;
import org.bsc.langgraph4j.agent._03_springai_agents_handoff.MultiAgentHandoffITest;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@Configuration
public class LangGraphStudioSampleConfig extends LangGraphStudioConfig {

    @SneakyThrows
    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        LangGraphStudioServer.Instance qAInstance = LangGraphStudioServer.Instance.builder()
                .title("Agent Q&A")
                .graph(new QAAssistant().getDebugGraph())
                .build();

        LangGraphStudioServer.Instance springaiAgentHandoff = LangGraphStudioServer.Instance.builder()
                .title("spring-ai-Agent-Handoff")
                .graph(new MultiAgentHandoffITest().createGraph())
                .build();

        LangGraphStudioServer.Instance subGraph = LangGraphStudioServer.Instance.builder()
                .title("sub-graph-interruption")
                .graph(AgenticWorkflowWithSubgraphInterruption.builder()
                        .checkpointSaver(new MemorySaver())
                        .createGraph())
                .build();

        //说明：如果要运行特定的实例，可在url中指定，类似
        //http://localhost:8080/?instance=subGraph

        return Map.of("default", qAInstance,
                "springAiAgentHandoff", springaiAgentHandoff,
                "subGraph", subGraph);
    }


}
