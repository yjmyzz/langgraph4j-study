package org.bsc.langgraph4j.agent._01_basic.controller;


public record ChatResponse(String sessionId, String agentMessage, boolean waitingForUser) {
}