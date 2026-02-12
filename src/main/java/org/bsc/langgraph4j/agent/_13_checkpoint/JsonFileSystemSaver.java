package org.bsc.langgraph4j.agent._13_checkpoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.Checkpoint;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

/**
 * 基于文件的 CheckpointSaver，将 checkpoint 列表持久化为 <b>.json</b> 文件。
 * <p>
 * 与 {@link org.bsc.langgraph4j.checkpoint.FileSystemSaver} 行为一致，但写入格式为纯 JSON，
 * 便于人工查看和外部工具处理。每个 RunnableConfig 对应一个文件：thread-&lt;threadId&gt;.json。
 * </p>
 */
public class JsonFileSystemSaver extends MemorySaver {

    public static final String EXTENSION = ".json";

    private final Path targetFolder;
    private final Gson gson;

    public JsonFileSystemSaver(Path targetFolder) {
        this(targetFolder, new GsonBuilder().serializeNulls().create());
    }

    public JsonFileSystemSaver(Path targetFolder, Gson gson) {
        this.targetFolder = Objects.requireNonNull(targetFolder, "targetFolder cannot be null");
        this.gson = gson != null ? gson : new GsonBuilder().serializeNulls().create();

        var dir = targetFolder.toFile();
        if (dir.exists()) {
            if (dir.isFile()) {
                throw new IllegalArgumentException(format("targetFolder '%s' must be a folder", targetFolder));
            }
        } else {
            if (!dir.mkdirs()) {
                throw new IllegalArgumentException(format("targetFolder '%s' cannot be created", targetFolder));
            }
        }
    }

    private String getBaseName(RunnableConfig config) {
        var threadId = config.threadId().orElse(BaseCheckpointSaver.THREAD_ID_DEFAULT);
        return format("thread-%s", threadId);
    }

    private Path getPath(RunnableConfig config) {
        return Paths.get(targetFolder.toString(), getBaseName(config) + EXTENSION);
    }

    private java.io.File getFile(RunnableConfig config) {
        return getPath(config).toFile();
    }

    private void serialize(LinkedList<Checkpoint> checkpoints, java.io.File outFile) throws IOException {
        Objects.requireNonNull(checkpoints, "checkpoints cannot be null");
        Objects.requireNonNull(outFile, "outFile cannot be null");

        List<CheckpointEntry> entries = checkpoints.stream()
                .map(cp -> new CheckpointEntry(
                        cp.getId(),
                        cp.getNodeId(),
                        cp.getNextNodeId(),
                        cp.getState()))
                .toList();

        try (Writer writer = Files.newBufferedWriter(outFile.toPath())) {
            gson.toJson(entries, writer);
        }
    }

    private void deserialize(java.io.File file, LinkedList<Checkpoint> result) throws IOException {
        Objects.requireNonNull(file, "file cannot be null");
        Objects.requireNonNull(result, "result cannot be null");

        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            var type = new TypeToken<List<CheckpointEntry>>() {}.getType();
            List<CheckpointEntry> entries = gson.fromJson(reader, type);
            if (entries != null) {
                for (CheckpointEntry e : entries) {
                    result.add(Checkpoint.builder()
                            .id(e.id())
                            .nodeId(e.nodeId())
                            .nextNodeId(e.nextNodeId())
                            .state(e.state())
                            .build());
                }
            }
        }
    }

    @Override
    protected LinkedList<Checkpoint> loadedCheckpoints(RunnableConfig config, LinkedList<Checkpoint> checkpoints) throws Exception {
        java.io.File targetFile = getFile(config);
        if (targetFile.exists() && checkpoints.isEmpty()) {
            deserialize(targetFile, checkpoints);
        }
        return checkpoints;
    }

    @Override
    protected void insertedCheckpoint(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint) throws Exception {
        serialize(checkpoints, getFile(config));
    }

    @Override
    protected void updatedCheckpoint(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint) throws Exception {
        insertedCheckpoint(config, checkpoints, checkpoint);
    }

    /**
     * 删除指定 config 对应的 .json checkpoint 文件。
     *
     * @param config 对应的 RunnableConfig
     * @return 文件存在且删除成功返回 true，否则 false
     */
    public boolean deleteFile(RunnableConfig config) {
        java.io.File targetFile = getFile(config);
        return targetFile.exists() && targetFile.delete();
    }

    /**
     * JSON 序列化用的条目，与 {@link Checkpoint} 字段一一对应。
     * state 为 {@link AgentState#data()} 的 Map，直接序列化为 JSON 对象。
     */
    private record CheckpointEntry(String id, String nodeId, String nextNodeId, Map<String, Object> state) {}
}
