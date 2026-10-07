package com.aiagent.agent.repository;

import com.aiagent.agent.entity.AgentResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentResponsibilityRepository
        extends JpaRepository<AgentResponsibility, Long> {

    List<AgentResponsibility>
    findByAgentVersionIdAndEnabledTrueOrderByPriorityAsc(Long agentVersionId);
}