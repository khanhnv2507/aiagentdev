package com.aiagent.agent.repository;

import com.aiagent.agent.entity.AgentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentVersionRepository
        extends JpaRepository<AgentVersion, Long> {

    Optional<AgentVersion> findByAgentCodeAndVersion(
            String agentCode,
            Integer version
    );
}