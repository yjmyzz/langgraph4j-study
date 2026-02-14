package org.bsc.langgraph4j.agent._13_checkpoint;

import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.FileSystemSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import org.bsc.langgraph4j.state.StateSnapshot;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.lang.System.out;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.prebuilt.MessagesState.MESSAGES_STATE;

/**
 * @author junmingyang
 */
public class CheckPointApplication {


    public static void main(String[] args) throws Exception {
        startWithoutCheckpoint();
        out.println("\n------------------------\n");

        BaseCheckpointSaver saver = getSaver();
        startWithCheckpoint(saver);
        out.println("\n------------------------\n");

        recoverFromCheckpoint(saver);
    }




    static void startWithoutCheckpoint() throws Exception {
        StateGraph<MessagesState<String>> graph = getGraph();

        //打印图的mermaid代码
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID, "Sequence Graph", true).content());


        graph.addBeforeCallNodeHook((String node, MessagesState<String> data, RunnableConfig config) -> {
            out.println("Before calling node: " + node + ", data: " + data.data());
            return CompletableFuture.completedFuture(data.data());
        });

        //node-3进入前，被打断
        CompileConfig cc = CompileConfig.builder()
                .interruptBefore("node-3")
                .build();

        RunnableConfig rc = RunnableConfig.builder()
                .threadId("test-interrupt")
                .build();

        CompiledGraph<MessagesState<String>> workflow = graph.compile(cc);

        //运行完后，最终只会输出[have a] - node-3被打断，执行中止
        workflow.invoke(Map.of(), rc)
                .ifPresent(state -> System.out.println(state.value(MESSAGES_STATE).orElse(null)));
    }

    static BaseCheckpointSaver getSaver() {
        return new MemorySaver();
//        return new FileSystemSaver(Path.of("output"), new ObjectStreamStateSerializer<>(MessagesState<String>::new));
//        return new JsonFileSystemSaver(Path.of("output"));
    }

    static void startWithCheckpoint(BaseCheckpointSaver saver) throws Exception {
        StateGraph<MessagesState<String>> graph = getGraph();

        graph.addBeforeCallNodeHook((String node, MessagesState<String> data, RunnableConfig config) -> {
            out.println("Before calling node: " + node + ", data: " + data.data());
            return CompletableFuture.completedFuture(data.data());
        });

        //node-3进入前，被打断
        CompileConfig cc = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptBefore("node-3")
                .build();

        RunnableConfig rc = RunnableConfig.builder().threadId("test-interrupt")
                .build();
        CompiledGraph<MessagesState<String>> workflow = graph.compile(cc);

        //运行完后，最终只会输出[have a] - node-3被打断，执行中止
        workflow.invoke(Map.of(), rc)
                .ifPresent(state -> System.out.println(state.value(MESSAGES_STATE).orElse(null)));
    }

    static void recoverFromCheckpoint(BaseCheckpointSaver saver) throws Exception {
        StateGraph<MessagesState<String>> graph = getGraph();
        graph.addBeforeCallNodeHook((String node, MessagesState<String> data, RunnableConfig config) -> {
            out.println("Before calling node: " + node + ", data: " + data.data());
            return CompletableFuture.completedFuture(data.data());
        });

        CompileConfig cc = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptBefore("node-3")
                .build();

        RunnableConfig rc = RunnableConfig.builder().threadId("test-interrupt")
                .build();
        CompiledGraph<MessagesState<String>> workflow = graph.compile(cc);

        //取出interrupt前的状态快照
        StateSnapshot<MessagesState<String>> snapshot = workflow.getState(rc);
        System.out.println("snapshot=>" + snapshot.state().data());

        //将图的状态，更新到interrupt前的状态快照
        RunnableConfig runnableConfig = workflow.updateState(rc, snapshot.state().data());

        //从断点恢复运行
        workflow.invoke(GraphInput.resume(), runnableConfig)
                .ifPresent(state -> System.out.println(state.value(MESSAGES_STATE).orElse(null)));
    }

    public static StateGraph<MessagesState<String>> getGraph() throws GraphStateException {
        return new StateGraph<>(MessagesState.SCHEMA, MessagesState<String>::new)
                .addNode("node-1", node_async(state -> Map.of(MESSAGES_STATE, "have")))
                .addNode("node-2", node_async(state -> Map.of(MESSAGES_STATE, "a")))
                .addNode("node-3", node_async(state -> Map.of(MESSAGES_STATE, "good")))
                .addNode("node-4", node_async(state -> Map.of(MESSAGES_STATE, "trip")))
                .addEdge(GraphDefinition.START, "node-1")
                .addEdge("node-1", "node-2")
                .addEdge("node-2", "node-3")
                .addEdge("node-3", "node-4")
                .addEdge("node-4", GraphDefinition.END);
    }
}
