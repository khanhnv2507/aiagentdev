package com.aiagent.agent.dto;

public record AgentVersionResponse(
        Long id,
        String agentCode,
        String agentName,
        Integer version,
        String status,
        String systemInstruction,
        String modelProvider,
        String modelName,
        AgentResponsibilitiesResponse responsibilities,
        AgentContractsResponse contracts
) {
}