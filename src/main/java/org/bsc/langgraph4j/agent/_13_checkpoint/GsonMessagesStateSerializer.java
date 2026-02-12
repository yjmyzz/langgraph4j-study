package org.bsc.langgraph4j.agent._13_checkpoint;

import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.serializer.plain_text.gson.GsonStateSerializer;

/**
 * GsonStateSerializer 为抽象类，无法直接实例化。
 * 本类为 MessagesState 提供具体实现，供 FileSystemSaver 使用。
 */
public class GsonMessagesStateSerializer extends GsonStateSerializer<MessagesState<String>> {

    public GsonMessagesStateSerializer() {
        super(MessagesState::new);
    }
}
