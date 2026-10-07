package com.aiagent.agent.dto;

import java.util.Map;

public record AgentContractsResponse(
        Map<String, Object> input,
        Map<String, Object> output,
        Map<String, Object> constraint,
        Map<String, Object> completion
) {
}