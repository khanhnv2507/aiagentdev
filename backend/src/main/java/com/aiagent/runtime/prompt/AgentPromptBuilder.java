package com.aiagent.runtime.prompt;

import com.aiagent.agent.dto.AgentContractsResponse;
import com.aiagent.agent.dto.AgentDefinition;
import com.aiagent.agent.dto.AgentResponsibilitiesResponse;
import com.aiagent.runtime.context.AgentContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentPromptBuilder {

    private final ObjectMapper objectMapper;

    public AgentPromptBuilder(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public AgentPrompt build(AgentContext context) {

        AgentDefinition definition =
                context.agentDefinition();

        AgentResponsibilitiesResponse responsibilities =
                definition.responsibilities();

        AgentContractsResponse contracts =
                definition.contracts();

        String systemPrompt = """
                %s

                [RESPONSIBILITIES]

                MUST DO:
                %s

                CAN DO:
                %s

                MUST NOT DO:
                %s

                [CONSTRAINT CONTRACT]
                %s

                [OUTPUT CONTRACT]
                %s

                [COMPLETION CONTRACT]
                %s
                """.formatted(
                definition.systemInstruction(),
                formatList(responsibilities.mustDo()),
                formatList(responsibilities.canDo()),
                formatList(responsibilities.mustNotDo()),
                formatJson(contracts.constraint()),
                formatJson(contracts.output()),
                formatJson(contracts.completion())
        );

        String userPrompt = """
                [CURRENT INPUT]

                Project ID: %s

                Specification:
                %s
                """.formatted(
                context.projectId(),
                context.specification()
        );

        return new AgentPrompt(
                systemPrompt,
                userPrompt
        );
    }

    private String formatList(
            List<String> items
    ) {
        return items.stream()
                .map(item -> "- " + item)
                .reduce(
                        "",
                        (left, right) ->
                                left.isEmpty()
                                        ? right
                                        : left + "\n" + right
                );
    }

    private String formatJson(Object value) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize contract to JSON",
                    e
            );
        }
    }
}