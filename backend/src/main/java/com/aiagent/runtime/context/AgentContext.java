package com.aiagent.runtime.context;

import com.aiagent.agent.dto.AgentDefinition;

public record AgentContext(
        AgentDefinition agentDefinition,
        Long projectId,
        String specification
) {
}