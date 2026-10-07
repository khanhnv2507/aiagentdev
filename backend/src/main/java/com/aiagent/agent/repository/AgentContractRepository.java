package com.aiagent.agent.repository;

import com.aiagent.agent.entity.AgentContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentContractRepository
        extends JpaRepository<AgentContract, Long> {

    List<AgentContract>
    findByAgentVersionIdAndEnabledTrueOrderByIdAsc(
            Long agentVersionId
    );
}