package com.aiagent.runtime.prompt;

public record AgentPrompt(
        String systemPrompt,
        String userPrompt
) {
}