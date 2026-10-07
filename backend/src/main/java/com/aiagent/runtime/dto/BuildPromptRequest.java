package com.aiagent.runtime.dto;

public record BuildPromptRequest(
        String agentCode,
        Integer agentVersion,
        Long projectId,
        String specification
) {
}