package com.aiagent.runtime.controller;

import com.aiagent.runtime.builder.AgentContextBuilder;
import com.aiagent.runtime.context.AgentContext;
import com.aiagent.runtime.dto.BuildPromptRequest;
import com.aiagent.runtime.prompt.AgentPrompt;
import com.aiagent.runtime.prompt.AgentPromptBuilder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runtime")
public class AgentRuntimeController {

    private final AgentContextBuilder agentContextBuilder;
    private final AgentPromptBuilder agentPromptBuilder;

    public AgentRuntimeController(
            AgentContextBuilder agentContextBuilder,
            AgentPromptBuilder agentPromptBuilder
    ) {
        this.agentContextBuilder = agentContextBuilder;
        this.agentPromptBuilder = agentPromptBuilder;
    }

    @PostMapping("/prompt")
    public AgentPrompt buildPrompt(
            @RequestBody BuildPromptRequest request
    ) {

        AgentContext context =
                agentContextBuilder.build(
                        request.agentCode(),
                        request.agentVersion(),
                        request.projectId(),
                        request.specification()
                );

        return agentPromptBuilder.build(context);
    }
}