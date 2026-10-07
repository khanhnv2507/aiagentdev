package com.aiagent.agent.dto;

public record AgentDefinition(
        Long id,
        String code,
        String name,
        Integer version,
        String status,
        String systemInstruction,
        String modelProvider,
        String modelName,
        AgentResponsibilitiesResponse responsibilities,
        AgentContractsResponse contracts
) {
}