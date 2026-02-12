package org.bsc.langgraph4j.agent._12_stream;

import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @author junmingyang
 */
public class StreamGraphApplication {

    public static void main(String[] args) throws GraphStateException {

        //流式模式
        StateGraph<MessagesState<String>> graph1 = getGraph();

        RunnableConfig rc = RunnableConfig.builder()
                .threadId("conversation-1")
                .streamMode(CompiledGraph.StreamMode.VALUES)
                .build();

        AsyncGenerator.Cancellable<NodeOutput<MessagesState<String>>> result = graph1.compile()
                //这里调用stream方法，而不是invoke方法
                .stream(Map.of(), rc);

        System.out.println("=========流式stream模式========");
        for (NodeOutput<MessagesState<String>> output : result) {
            System.out.println("Node: " + output.node());
            if (!CollectionUtils.isEmpty(output.state().messages())) {
                System.out.println(output.state().messages().toString());
            }

            //流式模式下，可以通过取消来停止流式执行
            if ("node-3".equalsIgnoreCase(output.node())) {
                result.cancel(true);
            }
        }

        System.out.println("=========常规invoke模式========");

        //常规模式
        StateGraph<MessagesState<String>> graph2 = getGraph();

        graph2.compile().invoke(Map.of()).ifPresent(c -> {
            System.out.println(c.data());
        });

    }


    public static StateGraph<MessagesState<String>> getGraph() throws GraphStateException {
        return new StateGraph<MessagesState<String>>(MessagesState.SCHEMA, MessagesState::new)
                .addNode("node-1", node_async(new ChatNode("1")))
                .addNode("node-2", node_async(new ChatNode(" 2")))
                .addNode("node-3", node_async(new ChatNode(" 3")))
                .addNode("node-4", node_async(new ChatNode(" 4")))
                .addNode("node-5", node_async(new ChatNode(" 5")))

                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-2", "node-3")
                .addEdge("node-3", "node-4")
                .addEdge("node-4", "node-5")
                .addEdge("node-5", GraphDefinition.END);
    }
}
