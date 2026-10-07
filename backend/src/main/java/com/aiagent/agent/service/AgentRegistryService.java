package com.aiagent.agent.service;

import com.aiagent.agent.dto.AgentContractsResponse;
import com.aiagent.agent.dto.AgentDefinition;
import com.aiagent.agent.dto.AgentResponsibilitiesResponse;
import com.aiagent.agent.entity.Agent;
import com.aiagent.agent.entity.AgentContract;
import com.aiagent.agent.entity.AgentResponsibility;
import com.aiagent.agent.entity.AgentVersion;
import com.aiagent.agent.repository.AgentContractRepository;
import com.aiagent.agent.repository.AgentRepository;
import com.aiagent.agent.repository.AgentResponsibilityRepository;
import com.aiagent.agent.repository.AgentVersionRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgentRegistryService {

    private final AgentRepository agentRepository;
    private final AgentVersionRepository agentVersionRepository;
    private final AgentResponsibilityRepository agentResponsibilityRepository;
    private final AgentContractRepository agentContractRepository;
    
    public AgentRegistryService(
            AgentRepository agentRepository,
            AgentVersionRepository agentVersionRepository,
            AgentResponsibilityRepository agentResponsibilityRepository,
            AgentContractRepository agentContractRepository
    ) {
        this.agentRepository = agentRepository;
        this.agentVersionRepository = agentVersionRepository;
        this.agentResponsibilityRepository = agentResponsibilityRepository;
        this.agentContractRepository = agentContractRepository;
    }

        public Agent getAgent(String code) {
                return agentRepository.findByCode(code)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agent not found: " + code
                                )
                        );
        }

        public AgentVersion getAgentVersion(String code, Integer version) {
                return agentVersionRepository
                        .findByAgentCodeAndVersion(code, version)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Agent version not found: "
                                                + code + " v" + version
                                )
                        );
        }

        public List<AgentResponsibility> getResponsibilities(Long agentVersionId) {
                return agentResponsibilityRepository
                        .findByAgentVersionIdAndEnabledTrueOrderByPriorityAsc(
                                agentVersionId
                        );
        }

        public List<AgentContract> getContracts(Long agentVersionId) {
                return agentContractRepository
                        .findByAgentVersionIdAndEnabledTrueOrderByIdAsc(
                                agentVersionId
                        );
        }

        public AgentDefinition loadAgentDefinition(String code, Integer version) {
                AgentVersion agentVersion =
                        getAgentVersion(code, version);

                List<AgentResponsibility> responsibilities =
                        getResponsibilities(agentVersion.getId());

                List<AgentContract> contracts =
                        getContracts(agentVersion.getId());

                List<String> mustDo = responsibilities.stream()
                        .filter(r -> "MUST_DO".equals(r.getResponsibilityType()))
                        .map(AgentResponsibility::getContent)
                        .toList();

                List<String> canDo = responsibilities.stream()
                        .filter(r -> "CAN_DO".equals(r.getResponsibilityType()))
                        .map(AgentResponsibility::getContent)
                        .toList();

                List<String> mustNotDo = responsibilities.stream()
                        .filter(r -> "MUST_NOT_DO".equals(r.getResponsibilityType()))
                        .map(AgentResponsibility::getContent)
                        .toList();

                AgentResponsibilitiesResponse responsibilityDefinition =
                        new AgentResponsibilitiesResponse(
                                mustDo,
                                canDo,
                                mustNotDo
                        );

                Map<String, Object> inputContract =
                        findContractContent(contracts, "INPUT");

                Map<String, Object> outputContract =
                        findContractContent(contracts, "OUTPUT");

                Map<String, Object> constraintContract =
                        findContractContent(contracts, "CONSTRAINT");

                Map<String, Object> completionContract =
                        findContractContent(contracts, "COMPLETION");

                AgentContractsResponse contractDefinition =
                        new AgentContractsResponse(
                                inputContract,
                                outputContract,
                                constraintContract,
                                completionContract
                        );

                return new AgentDefinition(
                        agentVersion.getId(),
                        agentVersion.getAgent().getCode(),
                        agentVersion.getAgent().getName(),
                        agentVersion.getVersion(),
                        agentVersion.getStatus(),
                        agentVersion.getSystemInstruction(),
                        agentVersion.getModelProvider(),
                        agentVersion.getModelName(),
                        responsibilityDefinition,
                        contractDefinition
                );
        }

        private Map<String, Object> findContractContent(List<AgentContract> contracts, String contractType) {
                return contracts.stream()
                        .filter(contract ->
                                contractType.equals(contract.getContractType())
                        )
                        .findFirst()
                        .map(AgentContract::getContent)
                        .orElse(null);
        }
}