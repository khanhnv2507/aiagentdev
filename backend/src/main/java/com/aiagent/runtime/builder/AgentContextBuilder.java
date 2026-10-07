package com.aiagent.runtime.builder;

import com.aiagent.agent.dto.AgentDefinition;
import com.aiagent.agent.service.AgentRegistryService;
import com.aiagent.runtime.context.AgentContext;

import org.springframework.stereotype.Component;

@Component
public class AgentContextBuilder {

    private final AgentRegistryService agentRegistryService;

    public AgentContextBuilder(
            AgentRegistryService agentRegistryService
    ) {
        this.agentRegistryService = agentRegistryService;
    }

    public AgentContext build(
            String agentCode,
            Integer agentVersion,
            Long projectId,
            String specification
    ) {
        AgentDefinition agentDefinition =
                agentRegistryService.loadAgentDefinition(
                        agentCode,
                        agentVersion
                );

        return new AgentContext(
                agentDefinition,
                projectId,
                specification
        );
    }
}