package org.bsc.langgraph4j.agent.studio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LangGraphStudioApplication {

    public static void main(String[] args) {
        // 设置默认使用studio配置
        System.setProperty("spring.profiles.active", "studio");
        SpringApplication.run(LangGraphStudioApplication.class, args);
    }

}