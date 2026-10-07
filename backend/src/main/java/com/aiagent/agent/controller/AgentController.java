package com.aiagent.agent.controller;

import com.aiagent.agent.dto.AgentDefinition;
import com.aiagent.agent.service.AgentRegistryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRegistryService agentRegistryService;

    public AgentController(
            AgentRegistryService agentRegistryService
    ) {
        this.agentRegistryService = agentRegistryService;
    }

    @GetMapping("/{code}/versions/{version}")
    public AgentDefinition getAgentVersion(
            @PathVariable String code,
            @PathVariable Integer version
    ) {
        return agentRegistryService.loadAgentDefinition(
                code,
                version
        );
    }
}