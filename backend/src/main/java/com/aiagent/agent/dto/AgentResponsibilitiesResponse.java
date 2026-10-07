package com.aiagent.agent.dto;

import java.util.List;

public record AgentResponsibilitiesResponse(
        List<String> mustDo,
        List<String> canDo,
        List<String> mustNotDo
) {
}