package org.bsc.langgraph4j.agent.studio;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.agent._01_basic.langgraph.QAAssistant;
import org.bsc.langgraph4j.agent._05_sequence.SequenceGraphApplication;
import org.bsc.langgraph4j.agent._06_conditional.ConditionalGraphApplication;
import org.bsc.langgraph4j.agent._07_parallel.ParallelGraphApplication;
import org.bsc.langgraph4j.agent._08_loop.LoopGraphApplication;
import org.bsc.langgraph4j.agent._09_human_in_loop.HumanInLoopGraphApplication;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@Configuration
public class LangGraphStudioSampleConfig extends LangGraphStudioConfig {

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        try {
            LangGraphStudioServer.Instance qAInstance = LangGraphStudioServer.Instance.builder()
                    .title("Agent Q&A")
                    .graph(new QAAssistant().getDebugGraph())
                    .build();

            //说明：如果要运行特定的实例，可在url中指定，类似
            //http://localhost:8080/?instance=conditional
            return Map.of("default", qAInstance,
                    "conditional", LangGraphStudioServer.Instance.builder()
                            .title("conditional graph")
                            .graph(ConditionalGraphApplication.getConditionalGraph())
                            .build(),
                    "sequence", LangGraphStudioServer.Instance.builder()
                            .title("sequence graph")
                            .graph(SequenceGraphApplication.getSequenceGraph())
                            .build(),
                    "parallel", LangGraphStudioServer.Instance.builder()
                            .title("parallel graph")
                            .graph(ParallelGraphApplication.getParallelGraph())
                            .build(),
                    "loop", LangGraphStudioServer.Instance.builder()
                            .title("loop graph")
                            .graph(LoopGraphApplication.getLoopGraph())
                            .build(),
                    "human_in_loop", LangGraphStudioServer.Instance.builder()
                            .title("human in loop graph")
                            .graph(HumanInLoopGraphApplication.getLoopGraph())
                            .build());
        } catch (GraphStateException e) {
            throw new IllegalStateException("Failed to build graph instances", e);
        }
    }


}
