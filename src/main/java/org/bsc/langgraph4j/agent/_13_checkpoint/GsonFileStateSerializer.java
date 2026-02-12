package org.bsc.langgraph4j.agent._13_checkpoint;

import org.bsc.langgraph4j.serializer.plain_text.gson.GsonStateSerializer;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AgentStateFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 使用 Gson 将 AgentState 序列化到指定文件，或从指定文件反序列化。
 * 可指定文件名进行读写。
 */
public class GsonFileStateSerializer extends GsonStateSerializer<AgentState> {

    private final String defaultFileName;

//    /**
//     * 使用给定的状态工厂创建序列化器，不指定默认文件名。
//     *
//     * @param stateFactory AgentState 工厂，例如 AgentState::new
//     */
//    public GsonFileStateSerializer(AgentStateFactory<AgentState> stateFactory) {
//        super(stateFactory);
//        this.defaultFileName = null;
//    }

    /**
     * 使用给定的状态工厂和默认文件名创建序列化器。
     *
     * @param stateFactory   AgentState 工厂，例如 AgentState::new
     * @param defaultFileName 默认文件名（或路径），在调用无参/单参方法时使用
     */
    public GsonFileStateSerializer(AgentStateFactory<AgentState> stateFactory, String defaultFileName) {
        super(stateFactory);
        this.defaultFileName = defaultFileName;
    }

    /**
     * 将 AgentState 序列化并写入指定文件。
     *
     * @param state   要序列化的状态
     * @param filePath 目标文件路径
     * @throws IOException 读写或序列化失败时抛出
     */
    public void saveToFile(AgentState state, String filePath) throws IOException {
        String json = writeDataAsString(state.data());
        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    /**
     * 将 AgentState 序列化并写入默认文件名对应的文件。
     *
     * @param state 要序列化的状态
     * @throws IOException 读写或序列化失败时抛出
     * @throws IllegalStateException 未指定默认文件名时抛出
     */
    public void saveToFile(AgentState state) throws IOException {
        if (defaultFileName == null) {
            throw new IllegalStateException("未指定文件名，请使用 saveToFile(state, filePath) 或构造时指定 defaultFileName");
        }
        saveToFile(state, defaultFileName);
    }

    /**
     * 从指定文件读取并反序列化为 AgentState。
     *
     * @param filePath 源文件路径
     * @return 反序列化后的 AgentState
     * @throws IOException 读写或反序列化失败时抛出
     */
    public AgentState loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        String json = Files.readString(path, StandardCharsets.UTF_8);
        return stateOf(readDataFromString(json));
    }

    /**
     * 从默认文件名对应的文件读取并反序列化为 AgentState。
     *
     * @return 反序列化后的 AgentState
     * @throws IOException 读写或反序列化失败时抛出
     * @throws IllegalStateException 未指定默认文件名时抛出
     */
    public AgentState loadFromFile() throws IOException {
        if (defaultFileName == null) {
            throw new IllegalStateException("未指定文件名，请使用 loadFromFile(filePath) 或构造时指定 defaultFileName");
        }
        return loadFromFile(defaultFileName);
    }
}
